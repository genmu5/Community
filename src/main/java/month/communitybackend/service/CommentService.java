package month.communitybackend.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import month.communitybackend.domain.Comment;
import month.communitybackend.domain.Post;
import month.communitybackend.domain.User;
import month.communitybackend.repository.CommentRepository;
import month.communitybackend.repository.PostRepository; // PostRepository 추가
import month.communitybackend.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepo;
    private final PostRepository postRepo; // PostRepository 주입
    private final UserRepository userRepo;

    @Transactional
    public Comment create(Long postId, String content) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User author = userRepo.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + username));

        // PostService 대신 PostRepository를 사용하여 Post 엔티티 직접 조회
        Post post = postRepo.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Post not found: " + postId));

        Comment comment = Comment.builder()
                .post(post)
                .author(author)
                .content(content)
                .build();
        return commentRepo.save(comment);
    }

    public List<Comment> listByPost(Long postId) {
        return commentRepo.findByPostId(postId);
    }

    public void delete(Long commentId) {
        commentRepo.deleteById(commentId);
    }

    @Transactional(readOnly = true)
    public Comment get(Long id) {
        return commentRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Comment not found. id =" + id));
    }

    @Transactional
    public Comment update(Long id, String newContent) {
        Comment c = get(id);
        c.setContent(newContent);
        return commentRepo.save(c);
    }
}