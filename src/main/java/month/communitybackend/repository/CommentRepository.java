package month.communitybackend.repository;

import month.communitybackend.domain.Comment;
import month.communitybackend.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByPostId(Long postId);
    List<Comment> findByAuthor(User author);
}
