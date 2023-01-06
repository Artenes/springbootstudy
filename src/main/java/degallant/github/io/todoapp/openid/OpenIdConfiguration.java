package degallant.github.io.todoapp.openid;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app.auth")
public record OpenIdConfiguration(String googleClientId) {

}
