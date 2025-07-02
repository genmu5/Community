package month.communitybackend.config;

import month.communitybackend.domain.Role;
import month.communitybackend.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner loadDefaultRoles(RoleRepository roleRepo) {
        return args -> {
            // ROLE_USER이 없으면 생성
            if (roleRepo.findByName("ROLE_USER").isEmpty()) {
                Role userRole = Role.builder()
                        .name("ROLE_USER")
                        .build();
                roleRepo.save(userRole);
            }
            // ROLE_ADMIN이 없으면 생성
            if (roleRepo.findByName("ROLE_ADMIN").isEmpty()) {
                Role adminRole = Role.builder()
                        .name("ROLE_ADMIN")
                        .build();
                roleRepo.save(adminRole);
            }
        };
    }
}