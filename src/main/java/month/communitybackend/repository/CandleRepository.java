package month.communitybackend.repository;

import month.communitybackend.domain.Candle;
import month.communitybackend.domain.CandleId;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CandleRepository extends JpaRepository<Candle, CandleId> {
    List<Candle> findByIdMarketOrderByIdOpenTimeDesc(String market, Pageable pageable);
}