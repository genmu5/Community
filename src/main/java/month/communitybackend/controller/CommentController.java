package month.communitybackend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Comment Document", description = "게시글 댓글 작성 API 문서화")
public class CommentController {
    private final CommentService commentService;

    @PostMapping
    @Operation(summary = "댓글 작성", description = "텍스트를 입력 받아 댓글로 등록합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "댓글 작성 성공")
    })
    public ResponseEntity<CommentDto.Response> create(
            @PathVariable Long postId,
            @RequestBody CommentDto.Create dto) {
        Comment c = commentService.create(postId, dto.getContent());
        return ResponseEntity.status(HttpStatus.CREATED).body(CommentDto.Response.from(c));
    }

    @GetMapping
    @Operation(summary = "댓글 조회", description = "게시글의 ID를 통해 해당 게시글의 댓글을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "댓글 조회 성공")
    })
    public List<CommentDto.Response> list(@PathVariable Long postId) {
        return commentService.listByPost(postId).stream()
                .map(CommentDto.Response::from)
                .toList();
    }

    @DeleteMapping("/{commentId}")
    @Operation(summary = "댓글 삭제", description = "게시글의 ID와 댓글의 ID를 통해 게시글의 댓글을 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "댓글 삭제 성공")
    })
    public ResponseEntity<Void> delete(
            @PathVariable Long postId,
            @PathVariable Long commentId) {
        commentService.delete(postId,commentId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{commentId}")
    @Operation(summary = "댓글 수정", description = "게시글의 ID와 댓글의 ID를 통해 게시글의 댓글을 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "댓글 수정 성공")
    })
    public ResponseEntity<CommentDto.Response> update(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @RequestBody CommentDto.Update dto) {
        Comment c = commentService.update(postId, commentId, dto.getContent());
        return ResponseEntity.ok(CommentDto.Response.from(c));
    }
}