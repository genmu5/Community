package month.communitybackend.config;

import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI opeAPI(){
        Info info = new Info()
                .title("Crypto API Document")
                .version("1.0")
                .description(
                        "암호화폐 커뮤니티 플랫폼 Crypto Community의 적용된 API 명세를 확인하는 사이트 입니다.\n")
                .contact(new
                        io.swagger.v3.oas.models.info.Contact().email("genmu5@ybm.co.kr"));

        String jwtScheme = "jwtAuth";
        SecurityRequirement securityRequirement = new SecurityRequirement().addList(jwtScheme);
        Components components = new Components()
            .addSecuritySchemes(jwtScheme, new SecurityScheme()
                .name("Authorization")
                .type(SecurityScheme.Type.HTTP)
                .in(SecurityScheme.In.HEADER)
                .scheme("Bearer")
                .bearerFormat("JWT"));

        return new OpenAPI()
                .addServersItem(new Server().url("http://localhost:8080"))
                .components(components)
                .info(info)
                .addSecurityItem(securityRequirement);
    }

}
