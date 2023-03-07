package degallant.github.io.todoapp;

import degallant.github.io.todoapp.authentication.ApiKeyEntity;
import degallant.github.io.todoapp.authentication.ApiKeyRepository;
import degallant.github.io.todoapp.domain.users.Role;
import degallant.github.io.todoapp.domain.users.UserEntity;
import degallant.github.io.todoapp.domain.users.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * @noinspection ClassCanBeRecord
 */
@Component
@RequiredArgsConstructor
public class DatabaseSeeder {

    private final ApiKeyRepository apiKeyRepository;
    private final UsersRepository usersRepository;

    @Bean
    public CommandLineRunner initialSeed(SeedConfiguration seedConfiguration, PasswordEncoder passwordEncoder) {
        return args -> {

            if (usersRepository.count() == 0) {
                var user = UserEntity.builder()
                        .email(seedConfiguration.adminEmail())
                        .password(passwordEncoder.encode(seedConfiguration.adminPassword()))
                        .name("root")
                        .role(Role.ROLE_ADMIN).build();
                usersRepository.save(user);
            }

            if (apiKeyRepository.count() == 0) {
                var id = UUID.fromString(seedConfiguration.apiKey());
                var key = new ApiKeyEntity();
                key.setId(id);
                key.setName("root");
                apiKeyRepository.save(key);
            }

        };
    }

}
