package degallant.github.io.todoapp;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app.seed")
public record SeedConfiguration(
        String adminEmail,
        String adminPassword,
        String apiKey
) {
}