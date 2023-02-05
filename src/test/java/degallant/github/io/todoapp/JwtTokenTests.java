package degallant.github.io.todoapp;

import degallant.github.io.todoapp.test.IntegrationTest;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class JwtTokenTests extends IntegrationTest {

    @Test
    public void authentication_fails_whenJwtTokenIsExpired() {

        var userId = authenticator.makeUser(DEFAULT_USER);
        var jwtToken = token.makeAccessTokenFor(userId, Instant.now().minus(1, ChronoUnit.MINUTES));
        request.withToken(jwtToken).to("tasks")
                .get().isUnauthorized()
                .hasField("$.type", contains("error.token_expired")).show();

    }

    @Test
    public void authentication_fails_whenJwtTokenIsEmpty() {
        //TODO
    }

    @Test
    public void authentication_fails_whenJwtTokenIsInvalid() {
        //TODO
    }

    @Test
    public void authentication_fails_whenJwtTokenIsTemperedWith() {
        //TODO
    }

    @Test
    public void refresh_fails_whenJwtTokenIsExpired() {
        //TODO
    }

    @Test
    public void refresh_fails_whenJwtTokenIsEmpty() {
        //TODO
    }

    @Test
    public void refresh_fails_whenJwtTokenIsInvalid() {
        //TODO
    }

    @Test
    public void refresh_fails_whenJwtTokenIsTemperedWith() {
        //TODO
    }

    @Test
    public void refresh_works_whenClientRequestNewSetOfTokens() {
        //TODO
    }

}
