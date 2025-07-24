package month.communitybackend.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import month.communitybackend.domain.Comment;
import month.communitybackend.domain.Post;
import month.communitybackend.domain.User;
import month.communitybackend.repository.CommentRepository;
import month.communitybackend.repository.PostRepository;
import month.communitybackend.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepo;
    private final PostRepository postRepo;
    private final UserRepository userRepo;

    @Transactional
    public Comment create(Long postId, String content) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User author = userRepo.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + username));

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

    public void delete(Long postId, Long commentId) {
        Comment comment = commentRepo.findById(commentId)
                        .orElseThrow(() -> new EntityNotFoundException("댓글을 찾을 수 없습니다."));
        if(!comment.getPost().getId().equals(postId)) {
            throw new IllegalArgumentException("해당 게시글의 작성되지 않은 댓글입니다.");
        }
        commentRepo.deleteById(commentId);
    }

    @Transactional(readOnly = true)
    public Comment get(Long id) {
        return commentRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Comment not found. id =" + id));
    }

    @Transactional
    public Comment update(Long postId, Long id, String newContent) {
        Comment c = get(id);
        if(!c.getPost().getId().equals(postId)) {
            throw new IllegalArgumentException("해당 게시글의 작성되지 않은 댓글입니다.");
        }
        c.setContent(newContent);
        return commentRepo.save(c);
    }
}