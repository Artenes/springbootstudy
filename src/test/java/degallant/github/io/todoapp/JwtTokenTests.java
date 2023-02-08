package degallant.github.io.todoapp;

import degallant.github.io.todoapp.test.IntegrationTest;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

public class JwtTokenTests extends IntegrationTest {

    @Test
    public void authentication_fails_whenJwtTokenIsExpired() {

        var userId = authenticator.makeUser(DEFAULT_USER);
        var jwtToken = token.make().withSubject(userId).withExpiresAt(Instant.now().minus(1, ChronoUnit.MINUTES)).build();
        request.withToken(jwtToken).to("tasks")
                .get().isBadRequest()
                .hasField("$.type", contains("error.token_expired"));

    }

    @Test
    public void authentication_fails_whenJwtTokenIsEmpty() {

        request.withToken("").to("tasks")
                .get().isForbidden();

    }

    @Test
    public void authentication_fails_whenJwtTokenIsInvalid() {

        request.withToken("invalid").to("tasks")
                .get().isBadRequest()
                .hasField("$.type", contains("error.invalid_token"));

    }

    @Test
    public void authentication_fails_whenJwtTokenIsTemperedWith() {

        var userId = authenticator.makeUser(DEFAULT_USER);
        var jwtToken = token.make().withSubject(userId).withIssuer("invalid-issuer").build();
        request.withToken(jwtToken).to("tasks")
                .get().isBadRequest()
                .hasField("$.type", contains("error.token_tempered"));

    }

    @Test
    public void authentication_fails_whenUserIsInvalid() {

        var jwtToken = token.make().withSubject("invalid").build();
        request.withToken(jwtToken).to("tasks")
                .get().isBadRequest()
                .hasField("$.type", contains("error.token_unknown_subject"));

    }

    @Test
    public void authentication_fails_whenUserIsUnknown() {

        var jwtToken = token.make().withSubject(UUID.randomUUID()).build();
        request.withToken(jwtToken).to("tasks")
                .get().isBadRequest()
                .hasField("$.type", contains("error.token_unknown_subject"));

    }

    @Test
    public void refresh_works_whenClientRequestNewSetOfTokens() {

        var userId = authenticator.makeUser(DEFAULT_USER);
        var jwtToken = token.make().withSubject(userId).asRefresh().build();
        request.withToken(jwtToken).to("auth/refresh")
                .get().isOk()
                .hasField("$.access_token", exists())
                .hasField("$.refresh_token", exists());

    }

}
