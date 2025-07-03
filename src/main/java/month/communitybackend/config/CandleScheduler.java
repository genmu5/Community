package month.communitybackend.config;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import month.communitybackend.dto.CandleDto;
import month.communitybackend.service.CandleService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class CandleScheduler {
    private final CandleService candleService;
    // 모니터링할 마켓 리스트
    private static final List<String> MARKETS = List.of("KRW-BTC", "KRW-ETH", "KRW-XRP");

    /** 1분마다 각 마켓의 1분봉 100개를 가져와 저장 */
    @Scheduled(fixedRate = 60_000)
    public void updateAllMarkets() {
        log.info(">> CandleScheduler running at {}", LocalDateTime.now());
        for (String m : MARKETS) {
            List<CandleDto> dto = candleService.fetchFromUpbit(m, 1, 100);
            candleService.saveAll(dto);
        }
    }
}