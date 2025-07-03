package month.communitybackend.domain;

import lombok.*;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "candles",
        indexes = @Index(name = "idx_market_opentime", columnList = "market, open_time"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Candle {
    @EmbeddedId
    private CandleId id;

    @Column(nullable = false)
    private BigDecimal open;

    @Column(nullable = false)
    private BigDecimal high;

    @Column(nullable = false)
    private BigDecimal low;

    @Column(nullable = false)
    private BigDecimal close;

    @Column(nullable = false)
    private BigDecimal volume;
}