package degallant.github.io.todoapp.authentication;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app.auth")
public record AuthenticationConfiguration(
        String signKey,
        String issuer,
        int accessExpiryMinutes,
        int refreshExpiryMinutes
) {
}
