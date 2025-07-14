package month.communitybackend.repository;

import month.communitybackend.domain.Post;
import month.communitybackend.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {
    Page<Post> findByMarket(String market, Pageable pageable);
    Page<Post> findByAuthor(User user, Pageable pageable);
}
