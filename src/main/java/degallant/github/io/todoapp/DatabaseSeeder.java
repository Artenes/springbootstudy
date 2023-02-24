package degallant.github.io.todoapp;

import degallant.github.io.todoapp.authentication.ApiKeyEntity;
import degallant.github.io.todoapp.authentication.ApiKeyRepository;
import degallant.github.io.todoapp.authentication.JwtToken;
import degallant.github.io.todoapp.domain.users.Role;
import degallant.github.io.todoapp.domain.users.UserEntity;
import degallant.github.io.todoapp.domain.users.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * @noinspection ClassCanBeRecord
 */
@Component
@RequiredArgsConstructor
public class DatabaseSeeder {

    private static final Logger log = LoggerFactory.getLogger(DatabaseSeeder.class);

    private final ApiKeyRepository apiKeyRepository;
    private final UsersRepository usersRepository;
    private final JwtToken jwtToken;

    @Bean
    public CommandLineRunner initialSeed() {
        return args -> {

            if (usersRepository.count() == 0) {
                var user = UserEntity.builder()
                        .email("admin@admin.com")
                        .name("Admin")
                        .role(Role.ROLE_ADMIN).build();
                usersRepository.save(user);
            }

            if (apiKeyRepository.count() == 0) {
                var key = ApiKeyEntity.builder()
                        .name("Root").build();
                apiKeyRepository.save(key);
            }

            var user = usersRepository.findByEmail("admin@admin.com");
            user.ifPresent(userEntity -> {

                var token = jwtToken.make().withSubject(userEntity.getId()).withExpiresAt(Instant.now().plus(1, ChronoUnit.DAYS)).build();
                log.debug("Access token for tests {}", token);

            });

            var key = apiKeyRepository.findByName("Root");
            key.ifPresent(apiKeyEntity -> {

                log.debug("API key for tests {}", apiKeyEntity.getId());

            });

        };
    }

}
