package month.communitybackend.controller;

import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@Tag(name = "Ticker Document", description = "실시간 시세 조회 API 문서화")
public class TickerController {

    private final RestTemplate rt = new RestTemplate();

    @GetMapping("/api/tickers")
    @Operation(summary = "실시간 시세 조회", description = "업비트 API를 통해 실시간 시세를 조회합니다. 쉼표로 구분된 마켓 코드(예: KRW-BTC,KRW-ETH)를 입력받습니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "시세 조회 성공")
    })
    public List<Map<String, Object>> getTickers(
            @RequestParam("markets") String marketsCsv
    ) {

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));


        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);


        String url = "https://api.upbit.com/v1/ticker?markets={markets}";
        ResponseEntity<List> resp = rt.exchange(
                url,
                HttpMethod.GET,
                requestEntity,
                List.class,
                marketsCsv
        );

        // 4) 그대로 반환
        return resp.getBody();
    }
}