package month.communitybackend.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import month.communitybackend.domain.PasswordResetToken;
import month.communitybackend.domain.Role;
import month.communitybackend.domain.User;
import month.communitybackend.dto.UserDto;
import month.communitybackend.repository.PasswordResetTokenRepository;
import month.communitybackend.repository.RoleRepository;
import month.communitybackend.repository.UserRepository;
import month.communitybackend.security.JwtTokenProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailService emailService;

    @Transactional
    public User register(UserDto.signupRequest requestDto) {
        if (userRepository.existsByUsername(requestDto.getUsername())) {
            throw new IllegalArgumentException("이미 사용 중인 사용자 이름입니다.");
        }
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));

        User user = User.builder()
                .username(requestDto.getUsername())
                .password(passwordEncoder.encode(requestDto.getPassword()))
                .email(requestDto.getEmail())
                .nickname(requestDto.getNickname())
                .build();
        user.getRoles().add(userRole);
        return userRepository.save(user);
    }

    @Transactional
    public Map<String, String> login(String username, String password) {
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(username, password);

        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);

        String accessToken = tokenProvider.createToken(authentication.getName(), authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).collect(Collectors.toList()));
        String refreshToken = tokenProvider.createRefreshToken(authentication.getName());

        return Map.of("accessToken", accessToken, "refreshToken", refreshToken);
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + username));
    }

    @Transactional
    public Map<String, String> refreshTokens(String refreshToken) {
        if (!tokenProvider.validateRefreshToken(refreshToken)) {
            throw new IllegalArgumentException("Invalid refresh token");
        }
        String username = tokenProvider.getUsername(refreshToken);
        User user = findByUsername(username);
        List<String> roles = user.getRoles().stream().map(Role::getName).collect(Collectors.toList());

        String newAccessToken = tokenProvider.createToken(username, roles);
        String newRefreshToken = tokenProvider.createRefreshToken(username);

        return Map.of("accessToken", newAccessToken, "refreshToken", newRefreshToken);
    }

    // 1. 비밀번호 재설정 요청 (이메일 전송)
    @Transactional
    public void requestPasswordReset(String username, String email) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("해당 아이디의 사용자를 찾을 수 없습니다."));

        if (!user.getEmail().equals(email)) {
            throw new IllegalArgumentException("아이디와 이메일이 일치하지 않습니다.");
        }

        // 기존에 발급된 토큰이 있다면 삭제 (선택 사항: 여러 번 요청 시 이전 토큰 무효화)
        passwordResetTokenRepository.findByUser(user).ifPresent(passwordResetTokenRepository::delete);

        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .expiryDate(LocalDateTime.now().plusHours(1)) // 1시간 유효
                .build();
        passwordResetTokenRepository.save(resetToken);

        emailService.sendPasswordResetEmail(user.getEmail(), token);
    }

    // 2. 비밀번호 재설정 완료
    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않거나 만료된 토큰입니다."));

        if (resetToken.isExpired()) {
            passwordResetTokenRepository.delete(resetToken); // 만료된 토큰 삭제
            throw new IllegalArgumentException("만료된 토큰입니다.");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword)); // 새 비밀번호 인코딩 후 저장
        userRepository.save(user);

        passwordResetTokenRepository.delete(resetToken); // 사용된 토큰 삭제
    }
}

