package month.communitybackend.repository;

import month.communitybackend.domain.PasswordResetToken;
import month.communitybackend.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(String token);
    Optional<PasswordResetToken> findByUser(User user); // 기존 토큰이 있는지 확인용
}
