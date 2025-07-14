package month.communitybackend.controller;

import java.util.List;
import java.util.Map;

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
public class TickerController {

    private final RestTemplate rt = new RestTemplate();

    @GetMapping("/api/tickers")
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
