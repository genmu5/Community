package month.communitybackend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Schema(description = "캔들 정보 DTO")
public class CandleDto {
    @Schema(description = "마켓 코드", example = "KRW-BTC")
    private String market;

    @JsonProperty("timestamp")
    @Schema(description = "캔들 타임스탬프 (UTC 기준)", example = "1672531200000")
    private long openTime;

    @JsonProperty("opening_price")
    @Schema(description = "시가", example = "51000000")
    private BigDecimal open;

    @JsonProperty("high_price")
    @Schema(description = "고가", example = "52000000")
    private BigDecimal high;

    @JsonProperty("low_price")
    @Schema(description = "저가", example = "50500000")
    private BigDecimal low;

    @JsonProperty("trade_price")
    @Schema(description = "종가", example = "51500000")
    private BigDecimal close;

    @JsonProperty("candle_acc_trade_volume")
    @Schema(description = "누적 거래량", example = "100.5")
    private BigDecimal volume;
}
