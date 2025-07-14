package month.communitybackend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.Set;

public class UserDto {
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class signupRequest{
        @NotBlank
        private String username;
        @NotBlank
        private String password;
        @Email
        private String email;
        private String nickname;
    }
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class LoginRequest{
        @NotBlank
        private String username;
        @NotBlank
        private String password;
    }
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Response{
        private Long id;
        private String username;
        private String email;
        private String nickname;
        private Set<String> roles;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Update {
        private String password;
        private String email;
    }
}
