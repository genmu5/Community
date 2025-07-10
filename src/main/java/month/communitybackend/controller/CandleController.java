package month.communitybackend.controller;

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
public class CandleController {
    private final CandleService candleService;
    @GetMapping
    public List<CandleDto> getCandles(
            @RequestParam(defaultValue = "KRW-BTC") String market,
            @RequestParam(defaultValue = "50") int limit
    ) {
        return candleService.getRecent(market, limit);
    }
}