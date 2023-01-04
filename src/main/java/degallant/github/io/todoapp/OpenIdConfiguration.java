package degallant.github.io.todoapp;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app.auth")
public record OpenIdConfiguration(String googleClientId) {

}
