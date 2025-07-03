package month.communitybackend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CandleDto {
    private String market;

    @JsonProperty("timestamp")
    private long openTime;

    @JsonProperty("opening_price")
    private BigDecimal open;

    @JsonProperty("high_price")
    private BigDecimal high;

    @JsonProperty("low_price")
    private BigDecimal low;

    /** Upbit API의 trade_price → 여기로 매핑됩니다 */
    @JsonProperty("trade_price")
    private BigDecimal close;

    /** Upbit API의 candle_acc_trade_volume → 여기로 매핑됩니다 */
    @JsonProperty("candle_acc_trade_volume")
    private BigDecimal volume;
}
