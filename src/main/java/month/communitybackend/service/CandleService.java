package month.communitybackend.service;

import lombok.RequiredArgsConstructor;
import month.communitybackend.domain.Candle;
import month.communitybackend.domain.CandleId;
import month.communitybackend.dto.CandleDto;
import month.communitybackend.repository.CandleRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import org.springframework.transaction.annotation.Transactional;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CandleService {
    private final CandleRepository candleRepo;
    private final RestTemplate rt = new RestTemplate();

    // 1분봉, 마켓, 개수 파라미터
    private static final String UPBIT_API =
            "https://api.upbit.com/v1/candles/minutes/{unit}?market={market}&count={count}";

    /**
     * 외부(업비트)에서 최근 count개 봉을 받아와 CandleDto로 변환
     */
    public List<CandleDto> fetchFromUpbit(String market, int unit, int count) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        HttpEntity<Void> req = new HttpEntity<>(headers);

        ResponseEntity<CandleDto[]> resp = rt.exchange(
                UPBIT_API,
                HttpMethod.GET,
                req,
                CandleDto[].class,
                unit,    // {unit}
                market,  // {market}
                count    // {count}
        );

        return Arrays.stream(resp.getBody())
                .map(d -> CandleDto.builder()
                        .market(market)
                        .openTime(d.getOpenTime())
                        .open(d.getOpen())
                        .high(d.getHigh())
                        .low(d.getLow())
                        .close(d.getClose())
                        .volume(d.getVolume())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * 받아온 DTO를 DB에 저장
     */
    @Transactional
    public void saveAll(List<CandleDto> dtos) {
        List<Candle> entities = dtos.stream()
                .map(d -> Candle.builder()
                        .id(new CandleId(d.getMarket(), d.getOpenTime()))
                        .open(d.getOpen())
                        .high(d.getHigh())
                        .low(d.getLow())
                        .close(d.getClose())
                        .volume(d.getVolume())
                        .build())
                .collect(Collectors.toList());
        candleRepo.saveAll(entities);
    }

    public List<CandleDto> getRecent(String market, int limit) {
        Pageable page = PageRequest.of(0, limit);
        return candleRepo
                .findByIdMarketOrderByIdOpenTimeDesc(market, page)
                .stream()
                .map(c -> CandleDto.builder()
                        .market(c.getId().getMarket())
                        .openTime(c.getId().getOpenTime())
                        .open(c.getOpen())
                        .high(c.getHigh())
                        .low(c.getLow())
                        .close(c.getClose())
                        .volume(c.getVolume())
                        .build())
                .collect(Collectors.toList());
    }
}
