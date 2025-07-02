package month.communitybackend.config;

import month.communitybackend.security.CustomUserDetailsService;
import month.communitybackend.security.JwtAuthenticationFilter;
import month.communitybackend.security.JwtTokenProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;

import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService userDetailsService;

    public SecurityConfig(
            JwtTokenProvider jwtTokenProvider,
            CustomUserDetailsService userDetailsService
    ) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userDetailsService = userDetailsService;
    }

    /** PasswordEncoder 빈 */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /** DaoAuthenticationProvider 빈 (UserDetailsService + PasswordEncoder) */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /** AuthenticationManager 빈 (login 처리용) */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /** JWT + URL별 인가 설정 */    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 우리가 만든 프로바이더 등록
                .authenticationProvider(authenticationProvider())

                // CSRF 끄고 세션 STATeless
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(m -> m.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 인증·인가 규칙
                .authorizeHttpRequests(auth -> auth
                        // 로그인/회원가입(토큰 발급) 경로
                        .requestMatchers("/api/auth/**").permitAll()
                        // 게시글 조회 및 댓글 조회만 공개
                        .requestMatchers(HttpMethod.GET, "/api/posts/**").permitAll()
                        // 나머지(게시글 작성∙수정∙삭제, 댓글 CRUD)는 인증 필요
                        .anyRequest().authenticated()
                )


                // 5) JWT 필터를 인증 필터 앞에 추가
                .addFilterBefore(
                        new JwtAuthenticationFilter(jwtTokenProvider),
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}
