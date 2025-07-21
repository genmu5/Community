package month.communitybackend.config;


import lombok.RequiredArgsConstructor;
import month.communitybackend.dto.CandleDto;
import month.communitybackend.service.CandleService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
@RequiredArgsConstructor
public class CandleScheduler {
    private final CandleService candleService;
    private static final List<String> MARKETS = List.of("KRW-BTC", "KRW-ETH", "KRW-XRP", "KRW-SOL", "KRW-SAND", "KRW-DOGE", "KRW-TRUMP", "KRW-STRIKE");

    /** 1분마다 각 마켓의 1분봉 100개를 가져와 저장 */
    @Scheduled(fixedRate = 60_000)
    public void updateAllMarkets() {
        for (String m : MARKETS) {
            List<CandleDto> dto = candleService.fetchFromUpbit(m, 1, 100);
            candleService.saveAll(dto);
        }
    }
}