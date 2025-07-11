package month.communitybackend.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import month.communitybackend.domain.RefreshToken;
import month.communitybackend.domain.Role;
import month.communitybackend.domain.User;
import month.communitybackend.dto.UserDto;
import month.communitybackend.repository.RefreshTokenRepository;
import month.communitybackend.repository.RoleRepository;
import month.communitybackend.repository.UserRepository;
import month.communitybackend.security.JwtTokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public User register(UserDto.signupRequest dto) {
        if (userRepo.findByUsername(dto.getUsername()).isPresent()) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }
        Role userRole = roleRepo.findByName("ROLE_USER")
                .orElseThrow(() -> new EntityNotFoundException("ROLE_USER 권한이 없습니다."));
        User user = User.builder()
                .username(dto.getUsername())
                .password(passwordEncoder.encode(dto.getPassword()))
                .email(dto.getEmail())
                .nickname(dto.getNickname())
                .roles(Collections.singleton(userRole))
                .build();
        return userRepo.save(user);
    }

    @Transactional
    public Map<String, String> login(String username, String password) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );
        User user = findByUsername(username);
        List<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList());

        String accessToken = jwtTokenProvider.createToken(user.getUsername(), roles);
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getUsername());

        refreshTokenRepository.findByUserId(user.getId())
                .ifPresentOrElse(
                        token -> token.update(refreshToken),
                        () -> refreshTokenRepository.save(new RefreshToken(user.getId(), refreshToken))
                );

        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", accessToken);
        tokens.put("refreshToken", refreshToken);
        return tokens;
    }

    @Transactional
    public Map<String, String> refreshTokens(String oldRefreshToken) {
        if (!jwtTokenProvider.validateRefreshToken(oldRefreshToken)) {
            throw new RuntimeException("Invalid refresh token");
        }

        RefreshToken refreshTokenEntity = refreshTokenRepository.findByRefreshToken(oldRefreshToken)
                .orElseThrow(() -> new RuntimeException("Refresh Token not found!"));

        User user = findById(refreshTokenEntity.getUserId());
        List<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList());

        String newAccessToken = jwtTokenProvider.createToken(user.getUsername(), roles);
        String newRefreshToken = jwtTokenProvider.createRefreshToken(user.getUsername());

        refreshTokenEntity.update(newRefreshToken);

        Map<String, String> newTokens = new HashMap<>();
        newTokens.put("accessToken", newAccessToken);
        newTokens.put("refreshToken", newRefreshToken);

        return newTokens;
    }

    public User findById(Long userId) {
        return userRepo.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("해당 회원이 존재하지 않습니다."));
    }

    public User findByUsername(String username) {
        return userRepo.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("해당 회원이 존재하지 않습니다."));
    }

    @Transactional
    public User updateProfile(Long userId, UserDto.signupRequest dto) {
        User user = findById(userId);
        user.setEmail(dto.getEmail());
        user.setNickname(dto.getNickname());
        return userRepo.save(user);
    }

    @Transactional
    public void delete(Long userId) {
        userRepo.deleteById(userId);
    }
}
