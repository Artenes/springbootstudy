package degallant.github.io.todoapp.authentication;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app.cors")
public record CorsAppConfiguration(
        String[] allowedOrigins,
        String[] allowedMethods,
        long maxAge,
        String[] allowedHeaders,
        String[] exposedHeaders
) {
}
