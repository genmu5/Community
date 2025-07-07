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
                .authorNickname(saved.getAuthor().getNickname())
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
        return postService.list(page, size)
                .map(p -> PostDto.Response.builder()
                        .id(p.getId())
                        .title(p.getTitle())
                        .content(p.getContent())
                        .authorNickname(p.getAuthor() != null ? p.getAuthor().getNickname() : "탈퇴한 사용자")
                        .market(p.getMarket())
                        .createdAt(p.getCreatedAt())
                        .updatedAt(p.getUpdatedAt())
                        .build()
                );
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostDto.Response> get(@PathVariable Long id) {
        Post p = postService.get(id);
        PostDto.Response res = PostDto.Response.builder()
                .id(p.getId())
                .title(p.getTitle())
                .content(p.getContent())
                .authorNickname(p.getAuthor() != null ? p.getAuthor().getNickname() : "탈퇴한 사용자")
                .market(p.getMarket())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
        return ResponseEntity.ok(res);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PostDto.Response> update(
            @PathVariable Long id,
            @Valid @RequestBody PostDto.Update dto
    ) {
        Post p = postService.update(id, dto.getTitle(), dto.getContent());
        PostDto.Response res = PostDto.Response.builder()
                .id(p.getId())
                .title(p.getTitle())
                .content(p.getContent())
                .authorNickname(p.getAuthor() != null ? p.getAuthor().getNickname() : "탈퇴한 사용자")
                .market(p.getMarket())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
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
