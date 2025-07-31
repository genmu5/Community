package month.communitybackend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import month.communitybackend.domain.Comment;
import month.communitybackend.domain.User;

import java.time.LocalDateTime;

public class CommentDto {
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    @Schema(description = "댓글 생성 요청 DTO")
    public static class Create{
        @NotBlank
        @Schema(description = "댓글 내용", example = "이 글 정말 유익하네요!")
        private String content;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    @Schema(description = "댓글 정보 응답 DTO")
    public static class Response{
        @Schema(description = "댓글 ID", example = "1")
        private Long id;
        @Schema(description = "댓글 내용", example = "이 글 정말 유익하네요!")
        private String content;
        @Schema(description = "작성자 닉네임", example = "코인고수")
        private String authorUsername;
        @Schema(description = "생성 시간")
        private LocalDateTime createdAt;

        public static Response from(Comment comment) {
            User author = comment.getAuthor();
            String username = (author != null) ? author.getUsername() : "탈퇴한 사용자";

            return Response.builder()
                    .id(comment.getId())
                    .content(comment.getContent())
                    .authorUsername(username)
                    .createdAt(comment.getCreatedAt())
                    .build();
        }
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    @Schema(description = "댓글 수정 요청 DTO")
    public static class Update{
        @NotBlank
        @Schema(description = "수정할 댓글 내용", example = "이 글 정말 유익하네요! 감사합니다.")
        private String content;
    }
}
