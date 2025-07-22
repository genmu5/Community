package month.communitybackend.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "password_reset_tokens")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token; // 사용자에게 이메일로 보낼 실제 토큰 값

    @OneToOne(targetEntity = User.class, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "user_id")
    private User user; // 토큰과 연결된 사용자

    @Column(nullable = false)
    private LocalDateTime expiryDate; // 토큰 만료 시간

    // 토큰이 유효한지 확인하는 헬퍼 메서드
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiryDate);
    }
}