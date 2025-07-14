package month.communitybackend.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import month.communitybackend.domain.Post;
import month.communitybackend.domain.User;
import month.communitybackend.dto.PostDto;
import month.communitybackend.repository.PostRepository;
import month.communitybackend.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepo;
    private final UserRepository userRepo;

    @Transactional
    public Post create(String title, String content, String market) {
        String username = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + username));

        Post post = Post.builder()
                .author(user)
                .title(title)
                .content(content)
                .market(market)
                .build();

        return postRepo.save(post);
    }

    public Page<PostDto.Response> list(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return postRepo.findAll(pageable).map(this::convertToPostDtoResponse);
    }

    public PostDto.Response get(Long postId) {
        Post post = postRepo.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Post not found"));
        return convertToPostDtoResponse(post);
    }

    @Transactional
    public Post update(Long postId, String title, String content) {
        Post post = postRepo.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Post not found"));
        post.setTitle(title);
        post.setContent(content);
        return postRepo.save(post);
    }

    @Transactional
    public void delete(Long postId) {
        postRepo.deleteById(postId);
    }

    public Page<PostDto.Response> getPostsByMarket(String market, Pageable pageable) {
        Page<Post> posts = postRepo.findByMarket(market, pageable);
        return posts.map(this::convertToPostDtoResponse);
    }

    public Page<PostDto.Response> getMyPosts(Pageable pageable) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + username));
        return postRepo.findByAuthor(user, pageable).map(this::convertToPostDtoResponse);
    }

    private PostDto.Response convertToPostDtoResponse(Post post) {
        return PostDto.Response.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .authorUsername(post.getAuthor() != null ? post.getAuthor().getUsername() : "탈퇴한 사용자")
                .market(post.getMarket())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }
}
