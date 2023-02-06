package degallant.github.io.todoapp.authentication;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@ConfigurationProperties("app.auth")
public record AuthenticationConfiguration(
        String signKey,
        String issuer,
        int accessExpiryMinutes,
        int refreshExpiryMinutes
) {

    public Instant accessExpiration() {
        return Instant.now().plus(accessExpiryMinutes, ChronoUnit.MINUTES);
    }

    public Instant refreshExpiration() {
        return Instant.now().plus(refreshExpiryMinutes, ChronoUnit.MINUTES);
    }

}
