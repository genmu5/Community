package month.communitybackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CommunityBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(CommunityBackendApplication.class, args);
    }

}
