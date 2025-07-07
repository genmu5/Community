package month.communitybackend.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import month.communitybackend.domain.RefreshToken;
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
import org.springframework.security.core.GrantedAuthority;
import org.springframework.transaction.annotation.Transactional;
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
    @Transactional
    public ResponseEntity<Map<String,String>> login(
            @Valid @RequestBody UserDto.LoginRequest dto
    ) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        dto.getUsername(), dto.getPassword()
                )
        );
        String accessToken = jwtTokenProvider.createToken(
                auth.getName(),
                auth.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .toList()
        );
        String refreshToken = jwtTokenProvider.createRefreshToken(auth.getName());

        User user = userRepository.findByUsername(auth.getName()).orElseThrow();

        refreshTokenRepository.findByUserId(user.getId())
                .ifPresentOrElse(
                        existingToken -> existingToken.update(refreshToken),
                        () -> refreshTokenRepository.save(new RefreshToken(user.getId(), refreshToken))
                );

        return ResponseEntity.ok(Map.of("accessToken", accessToken, "refreshToken", refreshToken));
    }

    @PostMapping("/refresh")
    public ResponseEntity<Map<String, String>> refresh(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");

        if (refreshToken == null || !jwtTokenProvider.validateRefreshToken(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String username = jwtTokenProvider.getUsername(refreshToken);
        User user = userRepository.findByUsername(username).orElseThrow();

        String newAccessToken = jwtTokenProvider.createToken(username, user.getRoles().stream().map(Role::getName).toList());

        return ResponseEntity.ok(Map.of("accessToken", newAccessToken));
    }

    @PostMapping("/logout")
    @Transactional
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        String username = jwtTokenProvider.getUsername(token);
        User user = userRepository.findByUsername(username).orElseThrow();
        refreshTokenRepository.findByUserId(user.getId()).ifPresent(refreshTokenRepository::delete);
        return ResponseEntity.ok().build();
    }
}
