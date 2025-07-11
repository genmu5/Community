package month.communitybackend.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import month.communitybackend.domain.Role;
import month.communitybackend.domain.User;
import month.communitybackend.dto.UserDto;
import month.communitybackend.repository.RefreshTokenRepository;
import month.communitybackend.repository.UserRepository;
import month.communitybackend.security.JwtTokenProvider;
import month.communitybackend.service.UserService;
import org.springframework.http.*;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    @PostMapping("/register")
    public ResponseEntity<UserDto.Response> register(
            @Valid @RequestBody UserDto.signupRequest dto
    ) {
        User created = userService.register(dto);

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
        Map<String, String> tokens = userService.login(loginRequest.getUsername(), loginRequest.getPassword());

        // Refresh Token을 쿠키에 담기
        Cookie refreshTokenCookie = new Cookie("refreshToken", tokens.get("refreshToken"));
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(false); // HTTPS 환경에서만 전송
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(60 * 60 * 24 * 14); // 2주
        // refreshTokenCookie.setSameSite("Strict"); // SameSite 설정

        response.addCookie(refreshTokenCookie);

        return ResponseEntity.ok(Map.of("accessToken", tokens.get("accessToken")));
    }
    @GetMapping("/me")
    public ResponseEntity<UserDto.Response> getCurrentUser(Authentication authentication) {
        if(authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String username = authentication.getName();
        User user = userService.findByUsername(username);

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
        Map<String, String> newTokens = userService.refreshTokens(refreshToken);

        Cookie newRefreshTokenCookie = new Cookie("refreshToken", newTokens.get("refreshToken"));
        newRefreshTokenCookie.setHttpOnly(true);
        newRefreshTokenCookie.setSecure(false);
        newRefreshTokenCookie.setPath("/");
        newRefreshTokenCookie.setMaxAge(60 * 60 * 24 * 14);
        response.addCookie(newRefreshTokenCookie);

        return ResponseEntity.ok(Map.of("accessToken", newTokens.get("accessToken")));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        Cookie refreshTokenCookie = new Cookie("refreshToken", null);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(false);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(0); // 쿠키 즉시 만료
        response.addCookie(refreshTokenCookie);
        return ResponseEntity.ok("Logged out successfully");
    }
}
