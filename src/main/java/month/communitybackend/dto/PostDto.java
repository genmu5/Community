package month.communitybackend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;


public class PostDto {
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "게시글 생성 요청 DTO")
    public static class Create {
        @NotBlank
        @Schema(description = "게시글 제목", example = "비트코인 전망에 대해 토론해봅시다.")
        private String title;
        @NotBlank
        @Schema(description = "게시글 내용", example = "최근 비트코인 가격 변동성이 큰데, 다들 어떻게 생각하시나요?")
        private String content;
        @NotBlank
        @Schema(description = "관련 코인 마켓 코드", example = "KRW-BTC")
        private String market;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "게시글 수정 요청 DTO")
    public static class Update {
        @NotBlank
        @Schema(description = "수정할 게시글 제목", example = "비트코인 단기 전망 공유")
        private String title;
        @NotBlank
        @Schema(description = "수정할 게시글 내용", example = "단기적으로는 상승할 것으로 보입니다.")
        private String content;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "게시글 상세 조회 응답 DTO")
    public static class Response {
        @Schema(description = "게시글 ID", example = "1")
        private Long id;
        @Schema(description = "게시글 제목", example = "비트코인 전망에 대해 토론해봅시다.")
        private String title;
        @Schema(description = "게시글 내용", example = "최근 비트코인 가격 변동성이 큰데, 다들 어떻게 생각하시나요?")
        private String content;
        @Schema(description = "작성자 닉네임", example = "코인왕")
        private String authorUsername;
        @Schema(description = "관련 코인 마켓 코드", example = "KRW-BTC")
        private String market;
        @Schema(description = "생성 시간")
        private LocalDateTime createdAt;
        @Schema(description = "수정 시간")
        private LocalDateTime updatedAt;
    }
}

