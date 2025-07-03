package month.communitybackend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;



public class PostDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Create {
        @NotBlank
        private String title;
        @NotBlank
        private String content;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Update {
        @NotBlank
        private String title;
        @NotBlank
        private String content;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long id;
        private String title;
        private String content;
    }
}

