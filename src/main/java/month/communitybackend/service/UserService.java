package month.communitybackend.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import month.communitybackend.domain.Role;
import month.communitybackend.domain.User;
import month.communitybackend.dto.UserDto;
import month.communitybackend.repository.RoleRepository;
import month.communitybackend.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final PasswordEncoder passwordEncoder;

    /**
     * 신규 회원 가입
     */
    public User register(UserDto.signupRequest dto) {
        // 사용자명 중복 검사
        if (userRepo.findByUsername(dto.getUsername()).isPresent()) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }

        // 기본 ROLE_USER 권한 조회
        Role userRole = roleRepo.findByName("ROLE_USER")
                .orElseThrow(() -> new EntityNotFoundException("ROLE_USER 권한이 없습니다."));

        // User 엔티티 생성 및 저장
        User user = User.builder()
                .username(dto.getUsername())
                .password(passwordEncoder.encode(dto.getPassword()))
                .email(dto.getEmail())
                .nickname(dto.getNickname())
                .roles(Collections.singleton(userRole))
                .build();

        return userRepo.save(user);
    }

    /**
     * ID로 회원 조회
     */
    public User findById(Long userId) {
        return userRepo.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("해당 회원이 존재하지 않습니다."));
    }

    /**
     * username 으로 회원 조회 (인증 처리 시 사용)
     */
    public User findByUsername(String username) {
        return userRepo.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("해당 회원이 존재하지 않습니다."));
    }

    /**
     * 회원 정보 수정 (이메일, 닉네임)
     * (signupRequest dto를 재활용해 email, nickname을 업데이트)
     */
    public User updateProfile(Long userId, UserDto.signupRequest dto) {
        User user = findById(userId);
        user.setEmail(dto.getEmail());
        user.setNickname(dto.getNickname());
        return userRepo.save(user);
    }

    /**
     * 회원 탈퇴
     */
    public void delete(Long userId) {
        userRepo.deleteById(userId);
    }
}
