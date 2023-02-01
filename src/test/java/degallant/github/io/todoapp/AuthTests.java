package degallant.github.io.todoapp;

import degallant.github.io.todoapp.common.IntegrationTest;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.reactive.server.JsonPathAssertions;

public class AuthTests extends IntegrationTest {

    @Test
    public void authenticate_failsWhenTokenIsNotProvided() {

        request.asGuest().to("auth")
                .withField("something_else", "random")
                .post().isBadRequest()
                .hasField("$.errors[0].type", v -> v.value(Matchers.containsString("validation.is_required")));

    }

    @Test
    public void authenticate_failsTokenIsEmpty() {

        request.asGuest().to("auth")
                .withField("open_id_token", "")
                .post().isBadRequest()
                .hasField("$.errors[0].type", v -> v.value(Matchers.containsString("validation.is_empty")));

    }

    @Test
    public void authenticate_registerNewUser() {

        String email = "email@gmail.com";
        String name = "Jhon Doe";
        String profileUrl = "https://google.com/profile/903jfiwfiwoe";

        String openIdToken = authenticator.makeTokenFor(email, name, profileUrl);

        var response = request.asGuest().to("auth")
                .withField("open_id_token", openIdToken)
                .post().isCreated()
                .hasField("$.access_token", JsonPathAssertions::exists)
                .hasField("$.access_token", JsonPathAssertions::exists)
                .getResponse();

        var userUri = response.headers().getLocation();
        var token = response.body().get("access_token").asText();

        request.withToken(token).to(userUri)
                .get().isOk()
                .hasField("$.email", v -> v.isEqualTo(email))
                .hasField("$.name", v -> v.isEqualTo(name))
                .hasField("$.picture_url", v -> v.isEqualTo(profileUrl))
                .hasField("$._links.self.href", JsonPathAssertions::exists)
                .hasField("$._links.tasks.href", JsonPathAssertions::exists)
                .hasField("$._links.projects.href", JsonPathAssertions::exists)
                .hasField("$._links.tags.href", JsonPathAssertions::exists);

    }

    @Test
    public void authenticate_loginExistingUser() {

        String email = "email@gmail.com";
        String name = "Jhon Doe";
        String profileUrl = "https://google.com/profile/903jfiwfiwoe";

        String token = authenticator.makeTokenFor(email, name, profileUrl);

        request.asGuest().to("auth")
                .withField("open_id_token", token)
                .post().isCreated();

        request.asGuest().to("auth")
                .withField("open_id_token", token)
                .post().isOk()
                .hasField("$.access_token", JsonPathAssertions::exists);

    }

}
