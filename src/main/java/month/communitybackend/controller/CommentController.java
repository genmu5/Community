package month.communitybackend.controller;

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
            @RequestBody CommentDto.Create dto) {
        Comment c = commentService.create(postId, dto.getContent());
        CommentDto.Response body = CommentDto.Response.builder()
                .id(c.getId())
                .content(c.getContent())
                .authorUsername(c.getAuthor().getUsername())
                .createdAt(c.getCreatedAt())
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @GetMapping
    public List<CommentDto.Response> list(@PathVariable Long postId) {
        return commentService.listByPost(postId).stream()
                .map(c -> CommentDto.Response.builder()
                        .id(c.getId())
                        .content(c.getContent())
                        .authorUsername(c.getAuthor().getUsername())
                        .createdAt(c.getCreatedAt())
                        .build())
                .toList();
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long postId,
            @PathVariable Long commentId) {
        commentService.delete(commentId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{commentId}")
    public ResponseEntity<CommentDto.Response> update(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @RequestBody CommentDto.Update dto) {
        Comment c = commentService.update(commentId, dto.getContent());
        CommentDto.Response body = CommentDto.Response.builder()
                .id(c.getId())
                .content(c.getContent())
                .authorUsername(c.getAuthor().getUsername())
                .createdAt(c.getCreatedAt())
                .build();
        return ResponseEntity.ok(body);
    }
}