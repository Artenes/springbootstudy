package degallant.github.io.todoapp;

import degallant.github.io.todoapp.test.IntegrationTest;
import org.junit.jupiter.api.Test;

import java.util.UUID;

public class AuthTests extends IntegrationTest {

    @Test
    public void authenticate_fails_whenOpenIdTokenIsNotProvided() {

        request.asGuest().to("auth")
                .withField("something_else", "random")
                .post().isBadRequest()
                .hasField("$.errors[0].type", contains("validation.is_required"));

    }

    @Test
    public void authenticate_fails_whenOpenIdTokenIsEmpty() {

        request.asGuest().to("auth")
                .withField("open_id_token", "")
                .post().isBadRequest()
                .hasField("$.errors[0].type", contains("validation.is_empty"));

    }

    @Test
    public void authenticate_failsWhenOpenIdTokenIsInvalid() {

        authenticator.makeTokenInvalid("invalid");

        request.asGuest().to("auth")
                .withField("open_id_token", "invalid")
                .post().isBadRequest()
                .hasField("$.errors[0].type", contains("validation.openid_invalid_token"));

    }

    @Test
    public void authenticate_failsWhenOpenIdTokenParsingFails() {

        authenticator.makeParsingFailsTo("invalid");

        request.asGuest().to("auth")
                .withField("open_id_token", "invalid")
                .post().isBadRequest()
                .hasField("$.errors[0].type", contains("validation.openid_extraction_failed"));

    }

    @Test
    public void authenticate_registerNewUser() {

        String email = "email@gmail.com";
        String name = "Jhon Doe";
        String profileUrl = "https://google.com/profile/903jfiwfiwoe";

        String openIdToken = authenticator.makeOpenIdTokenFor(email, name, profileUrl);

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

        String token = authenticator.makeOpenIdTokenFor(email, name, profileUrl);

        request.asGuest().to("auth")
                .withField("open_id_token", token)
                .post().isCreated();

        request.asGuest().to("auth")
                .withField("open_id_token", token)
                .post().isOk()
                .hasField("$.access_token", exists());

    }

    @Test
    public void access_restrictedToNotAuthEndpoints() {

        request.asGuest().to("tasks").get().isForbidden();
        request.asGuest().to("tags").get().isForbidden();
        request.asGuest().to("comments").get().isForbidden();
        request.asGuest().to("projects").get().isForbidden();

    }

    @Test
    public void patch_failsWhenNameIsEmpty() {

        request.asUser(DEFAULT_USER).to("auth/profile")
                .withField("name", "")
                .patch().isBadRequest()
                .hasField("$.errors[0].type", contains("validation.is_empty"));

    }

    @Test
    public void patch_failsWithInvalidPictureUrl() {

        request.asUser(DEFAULT_USER).to("auth/profile")
                .withField("picture_url", "invalid")
                .patch().isBadRequest()
                .hasField("$.errors[0].type", contains("validation.is_not_url"));

    }

    @Test
    public void patch_failsWhenRoleIsEmpty() {

        request.asUser("another@gmail.com").to("auth/profile")
                .withField("role", "")
                .patch().isBadRequest()
                .hasField("$.errors[0].type", contains("validation.is_not_role"));

    }

    @Test
    public void patch_failsWhenRoleIsInvalid() {

        request.asUser("another@gmail.com").to("auth/profile")
                .withField("role", "invalid")
                .patch().isBadRequest()
                .hasField("$.errors[0].type", contains("validation.is_not_role"));

    }

    @Test
    public void patch_failsWhenAdminAlreadyExists() {

        request.asUser(DEFAULT_USER).to("auth/profile")
                .withField("role", "ROLE_ADMIN")
                .patch().isOk();

        request.asUser("another@gmail.com").to("auth/profile")
                .withField("role", "ROLE_ADMIN")
                .patch().isBadRequest()
                .hasField("$.errors[0].type", contains("validation.cannot_assign"));

    }

    @Test
    public void patch_successChangesName() {

        request.asUser(DEFAULT_USER).to("auth/profile")
                .withField("name", "New name")
                .patch().isOk();

        request.asUser(DEFAULT_USER).to("auth/profile")
                .get().isOk()
                .hasField("$.name", isEqualTo("New name"));

    }

    @Test
    public void patch_successChangesProfileUrl() {

        request.asUser(DEFAULT_USER).to("auth/profile")
                .withField("name", "http://newimage/image.jpg")
                .patch().isOk();

        request.asUser(DEFAULT_USER).to("auth/profile")
                .get().isOk()
                .hasField("$.name", isEqualTo("http://newimage/image.jpg"));

    }

    @Test
    public void patch_successElevatesToAdmin() {

        request.asUser(DEFAULT_USER).to("auth/profile")
                .withField("role", "ROLE_ADMIN")
                .patch().isOk();

        request.asUser(DEFAULT_USER).to("auth/profile")
                .get().isOk()
                .hasField("$.role", isEqualTo("ROLE_ADMIN"));

    }

    @Test
    public void promote_failsWhenUserTryToAccess() {

        request.asUser(DEFAULT_USER).to("auth/promote")
                .patch().isForbidden();

    }

    @Test
    public void promote_failsWhenUserIdIsNotProvided() {

        request.asUser(DEFAULT_USER).to("auth/profile")
                .withField("role", "ROLE_ADMIN")
                .patch().isOk();

        request.asUser(DEFAULT_USER).to("auth/promote")
                .patch()
                .isBadRequest()
                .hasField("$.errors[?(@.field == 'user_id')].type", firstContains("validation.is_required"));

    }

    @Test
    public void promote_failsWhenUserIdIsEmpty() {

        request.asUser(DEFAULT_USER).to("auth/profile")
                .withField("role", "ROLE_ADMIN")
                .patch().isOk();

        request.asUser(DEFAULT_USER).to("auth/promote")
                .withField("user_id", "").patch()
                .isBadRequest()
                .hasField("$.errors[?(@.field == 'user_id')].type", firstContains("validation.is_uuid"));

    }

    @Test
    public void promote_failsWhenUserIdIsInvalid() {

        request.asUser(DEFAULT_USER).to("auth/profile")
                .withField("role", "ROLE_ADMIN")
                .patch().isOk();

        request.asUser(DEFAULT_USER).to("auth/promote")
                .withField("user_id", "invalid").patch()
                .isBadRequest()
                .hasField("$.errors[?(@.field == 'user_id')].type", firstContains("validation.is_uuid"));

    }

    @Test
    public void promote_failsWhenUserIdDoesNotExists() {

        request.asUser(DEFAULT_USER).to("auth/profile")
                .withField("role", "ROLE_ADMIN")
                .patch().isOk();

        request.asUser(DEFAULT_USER).to("auth/promote")
                .withField("user_id", UUID.randomUUID()).patch()
                .isBadRequest()
                .hasField("$.errors[?(@.field == 'user_id')].type", firstContains("validation.do_not_exist"));

    }

    @Test
    public void promote_failsWhenRoleIsNotProvided() {

        request.asUser(DEFAULT_USER).to("auth/profile")
                .withField("role", "ROLE_ADMIN")
                .patch().isOk();

        request.asUser(DEFAULT_USER).to("auth/promote")
                .patch()
                .isBadRequest()
                .hasField("$.errors[?(@.field == 'role')].type", firstContains("validation.is_required"));

    }

    @Test
    public void promote_failsWhenRoleIsEmpty() {

        request.asUser(DEFAULT_USER).to("auth/profile")
                .withField("role", "ROLE_ADMIN")
                .patch().isOk();

        request.asUser(DEFAULT_USER).to("auth/promote")
                .withField("role", "").patch()
                .isBadRequest()
                .hasField("$.errors[?(@.field == 'role')].type", firstContains("validation.is_not_role"));

    }

    @Test
    public void promote_failsWhenRoleIsInvalid() {

        request.asUser(DEFAULT_USER).to("auth/profile")
                .withField("role", "ROLE_ADMIN")
                .patch().isOk();

        request.asUser(DEFAULT_USER).to("auth/promote")
                .withField("role", "invalid").patch()
                .isBadRequest()
                .hasField("$.errors[?(@.field == 'role')].type", firstContains("validation.is_not_role"));

    }

    @Test
    public void promote_failsWhenUserTryToUpdateHimself() {

        request.asUser(DEFAULT_USER).to("auth/profile")
                .withField("role", "ROLE_ADMIN")
                .patch().isOk();

        var userId = request.asUser(DEFAULT_USER).to("auth/profile")
                .get().isOk().getBody().get("id").asText();

        request.asUser(DEFAULT_USER).to("auth/promote")
                .withField("user_id", userId).patch()
                .isBadRequest()
                .hasField("$.errors[?(@.field == 'user_id')].type", firstContains("validation.cannot_change_role_current_user"));

    }

    @Test
    public void promote_successUpdateOtherUserToAdmin() {

        request.asUser(DEFAULT_USER).to("auth/profile")
                .withField("role", "ROLE_ADMIN")
                .patch().isOk();

        var otherId = request.asUser("another@gmail.com").to("auth/profile")
                .get().isOk()
                .hasField("$.role", contains("ROLE_USER"))
                .getBody().get("id").asText();

        request.asUser(DEFAULT_USER).to("auth/promote")
                .withField("user_id", otherId)
                .withField("role", "ROLE_ADMIN").patch()
                .isOk();

        request.asUser("another@gmail.com").to("auth/profile")
                .get().isOk()
                .hasField("$.role", contains("ROLE_ADMIN"));

    }

}
