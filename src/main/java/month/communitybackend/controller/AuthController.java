package month.communitybackend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import month.communitybackend.domain.Role;
import month.communitybackend.domain.User;
import month.communitybackend.dto.UserDto;
import month.communitybackend.service.AuthService;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth Document", description = "회원가입, 로그인, 토큰 갱신과 같은 인증 API 문서화")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "회원 가입", description = "사용자 정보를 입력받아 회원가입을 처리합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "회원가입 성공", content = @Content(schema = @Schema(implementation = UserDto.Response.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터입니다.", content = @Content)
    })
    public ResponseEntity<UserDto.Response> register(
            @Valid @RequestBody UserDto.signupRequest dto
    ) {
        User created = authService.register(dto);

        UserDto.Response res = UserDto.Response.builder()
                .id(created.getId())
                .username(created.getUsername())
                .email(created.getEmail())
                .nickname(created.getNickname())
                .roles(created.getRoles().stream()
                        .map(Role::getName)
                        .collect(Collectors.toSet()))
                .build();

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(res);
    }

    @PostMapping("/login")
    @Operation(summary = "로그인", description = "아이디와 비밀번호로 로그인하고, Access Token과 Refresh Token(쿠키)을 발급합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(type = "object", example = "{\"accessToken\": \"(access token)\"}"))),
            @ApiResponse(responseCode = "401", description = "인증 실패 (아이디 또는 비밀번호 오류)", content = @Content)
    })
    public ResponseEntity<?> login(@RequestBody UserDto.LoginRequest loginRequest, HttpServletResponse response) {
        Map<String, String> tokens = authService.login(loginRequest.getUsername(), loginRequest.getPassword());

        Duration maxAge = loginRequest.isRememberMe() ? Duration.ofDays(14) : null; // 2주 또는 세션 쿠키

        ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", tokens.get("refreshToken"))
                .httpOnly(true)
                .secure(false) // 개발 환경에서는 false, 배포 환경에서는 true
                .path("/")
                .sameSite("Lax") // SameSite 설정
                .maxAge(maxAge != null ? maxAge : Duration.ofSeconds(-1)) // maxAge가 null이면 세션 쿠키
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());

        return ResponseEntity.ok(Map.of("accessToken", tokens.get("accessToken")));
    }

    @GetMapping("/me")
    @Operation(summary = "내 정보 확인", description = "현재 로그인된 사용자의 정보를 Access Token을 이용해 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "사용자 정보 조회 성공", content = @Content(schema = @Schema(implementation = UserDto.Response.class))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content)
    })
    public ResponseEntity<UserDto.Response> getCurrentUser(Authentication authentication) {
        if(authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String username = authentication.getName();
        User user = authService.findByUsername(username);

        UserDto.Response res = UserDto.Response.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .roles(user.getRoles().stream()
                        .map(Role::getName)
                        .collect(Collectors.toSet()))
                .build();

        return ResponseEntity.ok(res);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Access Token 재발급", description = "쿠키의 Refresh Token을 이용해 새로운 Access Token을 발급합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Access Token 재발급 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(type = "object", example = "{\"accessToken\": \"(new access token)\"}"))),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 Refresh Token", content = @Content)
    })
    public ResponseEntity<?> refresh(@CookieValue("refreshToken") String refreshToken, HttpServletResponse response) {
        Map<String, String> newTokens = authService.refreshTokens(refreshToken);

        ResponseCookie newRefreshTokenCookie = ResponseCookie.from("refreshToken", newTokens.get("refreshToken"))
                .httpOnly(true)
                .secure(false) // 개발 환경에서는 false, 배포 환경에서는 true
                .path("/")
                .sameSite("Lax") // SameSite 설정
                .maxAge(Duration.ofDays(14)) // 2주
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, newRefreshTokenCookie.toString());
        return ResponseEntity.ok(Map.of("accessToken", newTokens.get("accessToken")));
    }

    @PostMapping("/logout")
    @Operation(summary = "로그아웃", description = "사용자의 Refresh Token을 만료시키고 쿠키를 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그아웃 성공", content = @Content(schema = @Schema(example = "Logged out successfully"))),
    })
    public ResponseEntity<?> logout(@CookieValue(name = "refreshToken", required = false) String refreshTokenValue, HttpServletResponse response) {
        if (refreshTokenValue != null && !refreshTokenValue.isEmpty()) {
            try {
                authService.logout(refreshTokenValue);
            } catch (IllegalArgumentException e) {
            }
        }
        ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", null)
                .httpOnly(true)
                .secure(false) // 배포 환경에서는 true로 변경
                .path("/")
                .maxAge(0) // 쿠키 즉시 만료
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());
        return ResponseEntity.ok("Logged out successfully");
    }

    @PostMapping("/verify-username-for-password-reset")
    @Operation(summary = "비밀번호 재설정을 위한 사용자 확인", description = "아이디를 입력받아 가입된 사용자인지 확인하고, 마스킹된 이메일을 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "사용자 확인 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(type = "object", example = "{\"email\": \"tes***@example.com\"}"))),
            @ApiResponse(responseCode = "400", description = "아이디가 입력되지 않음", content = @Content),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음", content = @Content)
    })
    public ResponseEntity<?> verifyUsernameForPasswordReset(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        if(username == null || username.isEmpty()) {
            return ResponseEntity.badRequest().body("아이디를 입력해주세요");
        }
        try{
            User user = authService.findByUsername(username);
            String maskedEmail = maskEmail(user.getEmail());
            return ResponseEntity.ok(Map.of("email", maskedEmail));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("해당 아이디의 사용자를 찾을 수 없습니다.");
        }

    }
    // 비밀번호 재설정 요청 엔드포인트
    @PostMapping("/forgot-password")
    @Operation(summary = "비밀번호 재설정 요청", description = "사용자 아이디와 이메일로 비밀번호 재설정 링크를 요청합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "재설정 링크 전송 성공", content = @Content(schema = @Schema(example = "비밀번호 재설정 링크가 이메일로 전송되었습니다."))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (아이디 또는 이메일 불일치)", content = @Content)
    })
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String email = request.get("email");
        if (username == null || username.isEmpty() || email == null || email.isEmpty()) {
            return ResponseEntity.badRequest().body("아이디와 이메일 주소를 모두 입력해주세요.");
        }
        try {
            authService.requestPasswordReset(username, email);
            return ResponseEntity.ok("비밀번호 재설정 링크가 이메일로 전송되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 비밀번호 재설정 완료 엔드포인트
    @PostMapping("/reset-password")
    @Operation(summary = "비밀번호 재설정", description = "재설정 토큰과 새로운 비밀번호로 비밀번호를 변경합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "비밀번호 재설정 성공", content = @Content(schema = @Schema(example = "비밀번호가 성공적으로 재설정되었습니다."))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (유효하지 않은 토큰 또는 비밀번호)", content = @Content)
    })
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        String newPassword = request.get("newPassword");

        if (token == null || token.isEmpty() || newPassword == null || newPassword.isEmpty()) {
            return ResponseEntity.badRequest().body("토큰과 새 비밀번호를 모두 입력해주세요.");
        }

        try {
            authService.resetPassword(token, newPassword);
            return ResponseEntity.ok("비밀번호가 성공적으로 재설정되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return email;
        }
        int atIndex = email.indexOf("@");
        String localPart = email.substring(0, atIndex);
        String domainPart = email.substring(atIndex);

        if (localPart.length() <= 3) {
            return localPart.replaceAll(".", "*") + domainPart; // 모든 문자를 *로 마스킹
        }
        return localPart.substring(0, 3) + "***" + domainPart; // 앞 3글자만 보여주고 마스킹
    }
}