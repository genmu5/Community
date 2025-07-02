package month.communitybackend.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import month.communitybackend.domain.Post;
import month.communitybackend.domain.*;
import month.communitybackend.repository.PostRepository;
import month.communitybackend.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepo;
    private final UserRepository userRepo;

    public Post create(Long userId, String title, String content) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
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
