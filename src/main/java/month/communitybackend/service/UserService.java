package month.communitybackend.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import month.communitybackend.domain.Comment;
import month.communitybackend.domain.Post;
import month.communitybackend.domain.User;
import month.communitybackend.dto.UserDto;
import month.communitybackend.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;


    @Transactional
    // 사용자 정보 수정
    public User updateUser(UserDto.Update updateDto) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("해당 사용자를 찾을 수 없습니다.:" + username));

        // 비밀번호 업데이트
        if (updateDto.getPassword() != null && !updateDto.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(updateDto.getPassword()));
        }

        // 이메일 업데이트
        if (updateDto.getEmail() != null && !updateDto.getEmail().isEmpty()) {
            user.setEmail(updateDto.getEmail());
        }

        return userRepository.save(user);
    }
    // 사용자 탈퇴
    @Transactional
    public void deleteUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("해당 사용자를 찾을 수 없습니다.: " + username));

        // 사용자와 연관된 리프레시 토큰 삭제
        refreshTokenRepository.findByUser(user).ifPresent(refreshTokenRepository::delete);

        // 사용자와 연관된 비밀번호 재설정 토큰 삭제
        passwordResetTokenRepository.findByUser(user).ifPresent(passwordResetTokenRepository::delete);

        // 사용자가 작성한 게시글의 author를 null로 설정
        Page<Post> posts = postRepository.findByAuthor(user, Pageable.unpaged());
        posts.forEach(post -> {
            post.setAuthor(null);
            postRepository.save(post);
        });

        // 사용자가 작성한 댓글의 author를 null로 설정
        List<Comment> comments = commentRepository.findByAuthor(user);
        comments.forEach(comment -> {
            comment.setAuthor(null);
            commentRepository.save(comment);
        });

        userRepository.delete(user);
    }
}