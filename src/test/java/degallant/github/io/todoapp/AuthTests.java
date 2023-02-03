package degallant.github.io.todoapp;

import degallant.github.io.todoapp.test.IntegrationTest;
import org.junit.jupiter.api.Test;

public class AuthTests extends IntegrationTest {

    @Test
    public void authenticate_failsWhenTokenIsNotProvided() {

        request.asGuest().to("auth")
                .withField("something_else", "random")
                .post().isBadRequest()
                .hasField("$.errors[0].type", contains("validation.is_required"));

    }

    @Test
    public void authenticate_failsTokenIsEmpty() {

        request.asGuest().to("auth")
                .withField("open_id_token", "")
                .post().isBadRequest()
                .hasField("$.errors[0].type", contains("validation.is_empty"));

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
                .hasField("$.access_token", exists())
                .hasField("$.access_token", exists())
                .getResponse();

        var userUri = response.headers().getLocation();
        var token = response.body().get("access_token").asText();

        request.withToken(token).to(userUri)
                .get().isOk()
                .hasField("$.email", isEqualTo(email))
                .hasField("$.name", isEqualTo(name))
                .hasField("$.picture_url", isEqualTo(profileUrl))
                .hasField("$._links.self.href", exists())
                .hasField("$._links.tasks.href", exists())
                .hasField("$._links.projects.href", exists())
                .hasField("$._links.tags.href", exists());

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
                .hasField("$.access_token", exists());

    }

}
