package month.communitybackend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import month.communitybackend.domain.Post;
import month.communitybackend.dto.PostDto;
import month.communitybackend.service.PostService;
import org.springframework.data.domain.Page;
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
        Post saved = postService.create(dto.getTitle(), dto.getContent());
        Response body = Response.builder()
                .id(saved.getId())
                .title(saved.getTitle())
                .content(saved.getContent())
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
                .build();
        return ResponseEntity.ok(res);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        postService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
