
package month.communitybackend.service;

import month.communitybackend.dto.UserDto;
import month.communitybackend.domain.User;
import month.communitybackend.dto.UserDto;
import month.communitybackend.repository.RefreshTokenRepository;
import month.communitybackend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.StopWatch;

import java.util.Map;
import java.util.Optional;

@SpringBootTest
public class AuthServicePerformanceTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    private final String testUsername = "testuser";
    private final String testPassword = "password";
    private final String testEmail = "testuser@example.com";

    @BeforeEach
    void setUp() {
        // 기존 테스트 사용자 및 관련 리프레시 토큰 삭제
        Optional<User> userOptional = userRepository.findByUsername(testUsername);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            refreshTokenRepository.findByUser(user).ifPresent(refreshTokenRepository::delete);
            userRepository.delete(user);
        }

        // 테스트를 위한 사용자 생성
        UserDto.signupRequest request = UserDto.signupRequest.builder()
                .username(testUsername)
                .password(testPassword)
                .email(testEmail)
                .build();
        authService.register(request);
    }

    @Test
    void performanceTest() {
        StopWatch stopWatch = new StopWatch();

        // 1. 로그인 성능 측정
        stopWatch.start("Login");
        Map<String, String> tokens = authService.login(testUsername, testPassword);
        stopWatch.stop();

        String accessToken = tokens.get("accessToken");
        String refreshToken = tokens.get("refreshToken");

        // 2. 토큰 재발급 성능 측정
        stopWatch.start("Refresh Token");
        Map<String, String> newTokens = authService.refreshTokens(refreshToken);
        stopWatch.stop();

        String newAccessToken = newTokens.get("accessToken");

//        // 3. 로그아웃 성능 측정
//        stopWatch.start("Logout");
//        authService.logout(newAccessToken);
//        stopWatch.stop();

        System.out.println(stopWatch.prettyPrint());
    }
}
