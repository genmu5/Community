package month.communitybackend.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import month.communitybackend.domain.Role;
import month.communitybackend.domain.User;
import month.communitybackend.dto.UserDto;
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

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;

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
}

