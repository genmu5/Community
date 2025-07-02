package month.communitybackend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import month.communitybackend.domain.Comment;
import month.communitybackend.dto.CommentDto;
import month.communitybackend.service.CommentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts/{postId}/comments")
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<CommentDto.Response> create(
            @PathVariable Long postId,
            @Valid @RequestBody CommentDto.Create dto
    ) {
        Comment c = commentService.create(dto.getAuthorId(), postId, dto.getContent());
        CommentDto.Response res = CommentDto.Response.builder()
                .id(c.getId())
                .content(c.getContent())
                .authorUsername(c.getAuthor().getUsername())
                .createdAt(c.getCreatedAt())
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    @GetMapping
    public List<CommentDto.Response> list(@PathVariable Long postId) {
        return commentService.listByPost(postId).stream()
                .map(c -> CommentDto.Response.builder()
                        .id(c.getId())
                        .content(c.getContent())
                        .authorUsername(c.getAuthor().getUsername())
                        .createdAt(c.getCreatedAt())
                        .build()
                )
                .toList();
    }

    /** 단건 조회 */
    @GetMapping("/{id}")
    public ResponseEntity<CommentDto.Response> getOne(
            @PathVariable Long postId,
            @PathVariable Long id
    ) {
        Comment c = commentService.get(id);
        CommentDto.Response res = CommentDto.Response.builder()
                .id(c.getId())
                .content(c.getContent())
                .authorUsername(c.getAuthor().getUsername())
                .createdAt(c.getCreatedAt())
                .build();
        return ResponseEntity.ok(res);
    }

    /** 단건 업데이트 */
    @PutMapping("/{id}")
    public ResponseEntity<CommentDto.Response> update(
            @PathVariable Long postId,
            @PathVariable Long id,
            @Valid @RequestBody CommentDto.Update dto
    ) {
        Comment c = commentService.update(id, dto.getContent());
        CommentDto.Response res = CommentDto.Response.builder()
                .id(c.getId())
                .content(c.getContent())
                .authorUsername(c.getAuthor().getUsername())
                .createdAt(c.getCreatedAt())
                .build();
        return ResponseEntity.ok(res);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long postId, @PathVariable Long id) {
        commentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}