package month.communitybackend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import month.communitybackend.domain.Post;
import month.communitybackend.dto.PostDto;
import month.communitybackend.service.PostService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import month.communitybackend.dto.PostDto.Response;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;

    @PostMapping
    public ResponseEntity<Response> create(@RequestBody PostDto.Create dto) {
        Post saved = postService.create(dto.getTitle(), dto.getContent(), dto.getMarket());
        Response body = Response.builder()
                .id(saved.getId())
                .title(saved.getTitle())
                .content(saved.getContent())
                .market(saved.getMarket())
                .authorUsername(saved.getAuthor().getUsername()) // Changed to authorUsername
                .createdAt(saved.getCreatedAt())
                .updatedAt(saved.getUpdatedAt())
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @GetMapping
    public Page<PostDto.Response> list(
            @RequestParam(defaultValue="0") int page,
            @RequestParam(defaultValue="10") int size
    ) {
        return postService.list(page, size);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostDto.Response> get(@PathVariable Long id) {
        PostDto.Response p = postService.get(id); // Changed type to PostDto.Response
        return ResponseEntity.ok(p);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PostDto.Response> update(
            @PathVariable Long id,
            @Valid @RequestBody PostDto.Update dto
    ) {
        // postService.update는 Post 엔티티를 반환하므로, 다시 DTO로 변환해야 합니다.
        Post updatedPost = postService.update(id, dto.getTitle(), dto.getContent());
        Response res = Response.builder()
                .id(updatedPost.getId())
                .title(updatedPost.getTitle())
                .content(updatedPost.getContent())
                .authorUsername(updatedPost.getAuthor() != null ? updatedPost.getAuthor().getUsername() : "탈퇴한 사용자") // Changed to authorUsername
                .market(updatedPost.getMarket())
                .createdAt(updatedPost.getCreatedAt())
                .updatedAt(updatedPost.getUpdatedAt())
                .build();
        return ResponseEntity.ok(res);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        postService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/market/{market}")
    public ResponseEntity<Page<PostDto.Response>> getPostsByMarket(
            @PathVariable String market,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<PostDto.Response> posts = postService.getPostsByMarket(market, pageable);
        return ResponseEntity.ok(posts);
    }
}
