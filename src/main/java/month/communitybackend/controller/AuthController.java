package month.communitybackend.controller;

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
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
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
    public ResponseEntity<?> logout(HttpServletResponse response) {
        ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", null)
                .httpOnly(true)
                .secure(false) // 개발 환경에서는 false, 배포 환경에서는 true
                .path("/")
                //.sameSite("Lax") // SameSite 설정
                .maxAge(0) // 쿠키 즉시 만료
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());
        return ResponseEntity.ok("Logged out successfully");
    }

    @PostMapping("/verify-username-for-password-reset")
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