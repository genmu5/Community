package month.communitybackend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import month.communitybackend.dto.CandleDto;
import month.communitybackend.service.CandleService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/candles")
@RequiredArgsConstructor
@Tag(name = "Candle Chart Document", description = "비트코인 1분봉 차트 정보 조회 API")
public class CandleController {
    private final CandleService candleService;
    @GetMapping
    @Operation(summary = "1분봉 캔들차트 데이터 조회", description = "Upbit Open API를 통해 1분봉 데이터 호출하여 DB에 저장한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    public List<CandleDto> getCandles(
            @RequestParam(defaultValue = "KRW-BTC") String market,
            @RequestParam(defaultValue = "50") int limit
    ) {
        return candleService.getRecent(market, limit);
    }
}