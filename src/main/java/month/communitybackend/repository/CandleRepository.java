package month.communitybackend.repository;

import month.communitybackend.domain.Candle;
import month.communitybackend.domain.CandleId;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CandleRepository extends JpaRepository<Candle, CandleId> {
    // Pageable을 쓰면 두 번째 파라미터로 limit을 넘길 수 있습니다.
    List<Candle> findByIdMarketOrderByIdOpenTimeDesc(String market, Pageable pageable);
}