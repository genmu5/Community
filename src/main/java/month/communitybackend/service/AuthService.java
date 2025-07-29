package month.communitybackend.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import month.communitybackend.domain.PasswordResetToken;
import month.communitybackend.domain.RefreshToken;
import month.communitybackend.domain.Role;
import month.communitybackend.domain.User;
import month.communitybackend.dto.UserDto;
import month.communitybackend.repository.PasswordResetTokenRepository;
import month.communitybackend.repository.RefreshTokenRepository;
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
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    // 회원 가입 요청
    public User register(UserDto.signupRequest requestDto) {
        if (userRepository.existsByUsername(requestDto.getUsername())) {
            throw new IllegalArgumentException("이미 사용 중인 사용자 이름입니다.");
        }
        // 사용자의 권한확인, 추후 관리자 등록도 고려하여 설계
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("권한이 없는 사용자 입니다."));

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
    // 로그인 요청
    public Map<String, String> login(String username, String password) {
        // username과 password를 기반으로 AuthenticationToken 객체 생성
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(username, password);

        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);

        // 인증 정보를 기반으로 JWT 토큰 생성
        User user = findByUsername(username);

        // Access Token, Refresh Token 생성
        String accessToken = tokenProvider.createToken(authentication.getName(), authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).collect(Collectors.toList()));
        String refreshToken = tokenProvider.createRefreshToken(authentication.getName());

        // Refresh Token 저장 또는 업데이트
        refreshTokenRepository.findByUser(user).ifPresentOrElse(
                refreshTokenEntity -> refreshTokenEntity.update(refreshToken),
                () -> refreshTokenRepository.save(new RefreshToken(user, refreshToken))
        );

        return Map.of("accessToken", accessToken, "refreshToken", refreshToken);
    }

    // 로그아웃 요청
    @Transactional
    public void logout(String refreshToken){
        RefreshToken refreshTokenEntity = refreshTokenRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 리프레쉬 토큰입니다."));
        refreshTokenRepository.delete(refreshTokenEntity);
    }

    // 사용자에 대한 에러 예외처리를 위해 재정의
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("해당 사용자를 찾을 수 없습니다.: " + username));
    }

    // 토큰 갱신 (RTR : Refresh Token Rotation)방식
    @Transactional
    public Map<String, String> refreshTokens(String refreshToken) {
        RefreshToken refreshTokenEntity = refreshTokenRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new IllegalArgumentException("허가되지 않거나 기간이 지난 Refresh token 입니다. 재로그인해주세요."));

        if (!tokenProvider.validateRefreshToken(refreshTokenEntity.getRefreshToken())) {
            refreshTokenRepository.delete(refreshTokenEntity);
            throw new IllegalArgumentException("허가되지 않거나 기간이 지난 Refresh token 입니다. 재로그인해주세요.");
        }

        String username = tokenProvider.getUsername(refreshTokenEntity.getRefreshToken());
        User user = refreshTokenEntity.getUser();
        List<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList());

        // AccessToken을 발급받을시 RefreshToken 또한 같이 재발급, RTR
        String newAccessToken = tokenProvider.createToken(username, roles);
        String newRefreshToken = tokenProvider.createRefreshToken(username);

        // DB의 Refresh Token 갱신
        refreshTokenEntity.update(newRefreshToken);

        return Map.of("accessToken", newAccessToken, "refreshToken", newRefreshToken);
    }

    // 비밀번호 재설정 요청 (이메일 전송)
    @Transactional
    public void requestPasswordReset(String username, String email) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("해당 아이디의 사용자를 찾을 수 없습니다."));

        if (!user.getEmail().equals(email)) {
            throw new IllegalArgumentException("아이디와 이메일이 일치하지 않습니다.");
        }

        String token = UUID.randomUUID().toString();
        LocalDateTime expiryDate = LocalDateTime.now().plusHours(1);

        PasswordResetToken resetToken = passwordResetTokenRepository.findByUser(user)
                        .orElse(new PasswordResetToken());

        resetToken.setUser(user);
        resetToken.setToken(token);
        resetToken.setExpiryDate(expiryDate);

        passwordResetTokenRepository.save(resetToken);

        emailService.sendPasswordResetEmail(user.getEmail(), token);
    }

    // 비밀번호 재설정 완료
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

