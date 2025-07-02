package month.communitybackend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;

public class CommentDto {
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Create{
        private Long authorId;
        @NotBlank private String content;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Response{
        private Long id;
        private String content;
        private String authorUsername;
        private LocalDateTime createdAt;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Update{
        @NotBlank
        private String content;
    }
}
