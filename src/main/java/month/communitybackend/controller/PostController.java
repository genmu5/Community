package month.communitybackend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Post Document", description = "게시글 작성 API 문서화")
public class PostController {
    private final PostService postService;

    @PostMapping
    @Operation(summary = "게시글 작성", description = "텍스트를 입력 받아 게시글로 등록합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "게시글 작성 성공")
    })
    public ResponseEntity<Response> create(@RequestBody PostDto.Create dto) {
        Post saved = postService.create(dto.getTitle(), dto.getContent(), dto.getMarket());
        Response body = Response.builder()
                .id(saved.getId())
                .title(saved.getTitle())
                .content(saved.getContent())
                .market(saved.getMarket())
                .authorUsername(saved.getAuthor().getUsername())
                .createdAt(saved.getCreatedAt())
                .updatedAt(saved.getUpdatedAt())
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @GetMapping
    @Operation(summary = "게시글 조회", description = "게시글을 페이지 단위로 게시글을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Size 만큼의 갯수를 가진 페이지를 조회 성공 ")
    })
    public Page<PostDto.Response> list(
            @RequestParam(defaultValue="0") int page,
            @RequestParam(defaultValue="10") int size
    ) {
        return postService.list(page, size);
    }

    @GetMapping("/{id}")
    @Operation(summary = "단일 게시글 조회", description = "게시글 ID를 통해 하나의 게시글의 대한 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "게시글 조회 성공")
    })
    public ResponseEntity<PostDto.Response> get(@PathVariable Long id) {
        PostDto.Response p = postService.get(id);
        return ResponseEntity.ok(p);
    }

    @PutMapping("/{id}")
    @Operation(summary = "게시글 내용 수정", description = "게시글의 내용을 수정합니다.")
    public ResponseEntity<PostDto.Response> update(
            @PathVariable Long id,
            @Valid @RequestBody PostDto.Update dto
    ) {
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
    @Operation(summary = "게시글 삭제", description = "게시글 ID를 통해 게시글을 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "게시글 삭제 성공 ")
    })
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        postService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/market/{market}")
    @Operation(summary = "시장별 게시글 조회", description = "특정 시장(예: BTC, ETH)에 해당하는 게시글을 페이지 단위로 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "시장별 게시글 조회 성공")
    })
    public ResponseEntity<Page<PostDto.Response>> getPostsByMarket(
            @PathVariable String market,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<PostDto.Response> posts = postService.getPostsByMarket(market, pageable);
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/my")
    @Operation(summary = "내 게시글 조회", description = "현재 로그인한 사용자가 작성한 게시글을 페이지 단위로 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "내 게시글 조회 성공")
    })
    public ResponseEntity<Page<PostDto.Response>> getMyPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<PostDto.Response> posts = postService.getMyPosts(pageable);
        return ResponseEntity.ok(posts);
    }
}