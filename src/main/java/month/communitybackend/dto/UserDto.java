package month.communitybackend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.Set;

public class UserDto {
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    @Schema(description = "회원가입 요청 DTO")
    public static class signupRequest{
        @NotBlank
        @Schema(description = "사용자 아이디", example = "testuser")
        private String username;
        @NotBlank
        @Schema(description = "비밀번호", example = "password123!")
        private String password;
        @Email
        @Schema(description = "이메일", example = "test@example.com")
        private String email;
        @Schema(description = "닉네임", example = "코인마스터")
        private String nickname;
    }
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    @Schema(description = "로그인 요청 DTO")
    public static class LoginRequest{
        @NotBlank
        @Schema(description = "사용자 아이디", example = "testuser")
        private String username;
        @NotBlank
        @Schema(description = "비밀번호", example = "password123!")
        private String password;
        @Schema(description = "로그인 상태 유지 여부", example = "true")
        private boolean rememberMe;
    }
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    @Schema(description = "사용자 정보 응답 DTO")
    public static class Response{
        @Schema(description = "사용자 고유 ID", example = "1")
        private Long id;
        @Schema(description = "사용자 아이디", example = "testuser")
        private String username;
        @Schema(description = "이메일", example = "test@example.com")
        private String email;
        @Schema(description = "닉네임", example = "코인마스터")
        private String nickname;
        @Schema(description = "사용자 권한", example = "[ROLE_USER]")
        private Set<String> roles;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    @Schema(description = "사용자 정보 수정 요청 DTO")
    public static class Update {
        @Schema(description = "새 비밀번호", example = "newPassword123!")
        private String password;
        @Schema(description = "새 이메일", example = "new@example.com")
        private String email;
    }
}
