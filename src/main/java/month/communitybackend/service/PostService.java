package month.communitybackend.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import month.communitybackend.domain.Post;
import month.communitybackend.domain.*;
import month.communitybackend.repository.PostRepository;
import month.communitybackend.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepo;
    private final UserRepository userRepo;

    @Transactional
    public Post create(String title, String content) {
        // ① SecurityContext에서 현재 로그인한 username 가져오기
        String username = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        // ② username으로 User 조회
        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + username));

        // ③ Post 생성 시 author에 위 user 연결
        Post post = Post.builder()
                .author(user)
                .title(title)
                .content(content)
                .build();

        return postRepo.save(post);
    }

    public Page<Post> list(int page, int size) {
        return postRepo.findAll(
                PageRequest.of(page, size, Sort.by("createdAt").descending())
        );
    }

    public Post get(Long postId) {
        return postRepo.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Post not found"));
    }

    public Post update(Long postId, String title, String content) {
        Post post = get(postId);
        post.setTitle(title);
        post.setContent(content);
        return postRepo.save(post);
    }

    public void delete(Long postId) {
        postRepo.deleteById(postId);
    }
}
