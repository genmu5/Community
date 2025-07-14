package month.communitybackend.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import month.communitybackend.domain.User;
import month.communitybackend.dto.UserDto;
import month.communitybackend.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User updateUser(UserDto.Update updateDto) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + username));

        // 비밀번호 업데이트 (제공된 경우)
        if (updateDto.getPassword() != null && !updateDto.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(updateDto.getPassword()));
        }

        // 이메일 업데이트 (제공된 경우)
        if (updateDto.getEmail() != null && !updateDto.getEmail().isEmpty()) {
            user.setEmail(updateDto.getEmail());
        }

        return userRepository.save(user);
    }

    @Transactional
    public void deleteUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + username));
        userRepository.delete(user);
    }
}