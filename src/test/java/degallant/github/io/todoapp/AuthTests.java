package degallant.github.io.todoapp;

import degallant.github.io.todoapp.test.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class AuthTests extends IntegrationTest {

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
        makeAdmin();
    }

    @Test
    public void all_fails_user_authentication_when_deleted() {

        var body = request.asUser(DEFAULT_USER).to("auth/profile").get().isOk().getBody();
        var id = body.get("id").asText();

        request.asUser(ADMIN_USER).to("admin/users/" + id).delete().isNoContent();

        try {
            request.asUser(DEFAULT_USER).to("auth/profile").get();
        } catch (AssertionError error) {
            var expected = "failed with status 409 CONFLICT";
            assertTrue(error.getMessage().contains(expected), "The message " + expected + " was not found in " + error.getMessage());
        }

    }

    @Test
    public void all_fails_token_belongs_to_deleted_user() {

        var body = request.asGuest().to("auth")
                .withField("open_id_token", authenticator.makeOpenIdTokenFor(DEFAULT_USER))
                .post().isCreated().getBody();
        var token = body.get("access_token").asText();

        var id = request.asUser(DEFAULT_USER).to("auth/profile").get().getBody().get("id").asText();

        request.asUser(ADMIN_USER).to("admin/users/" + id).delete().isNoContent();

        request.withToken(token).to("auth/profile")
                .get().isBadRequest().hasField("$.type", contains("error.token_unknown_subject"));

    }

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
    public void promote_failsWhenUserTryToAccess() {

        request.asUser(DEFAULT_USER).to("admin/promote")
                .patch().isForbidden();

    }

    @Test
    public void promote_failsWhenUserIdIsNotProvided() {

        request.asUser(ADMIN_USER).to("admin/promote")
                .withField("another", "something")
                .post().isBadRequest()
                .hasField("$.errors[?(@.field == 'user_id')].type", firstContains("validation.is_required"));

    }

    @Test
    public void promote_failsWhenUserIdIsEmpty() {

        request.asUser(ADMIN_USER).to("admin/promote")
                .withField("user_id", "")
                .post().isBadRequest()
                .hasField("$.errors[?(@.field == 'user_id')].type", firstContains("validation.is_uuid"));

    }

    @Test
    public void promote_failsWhenUserIdIsInvalid() {

        request.asUser(ADMIN_USER).to("admin/promote")
                .withField("user_id", "invalid").post()
                .isBadRequest()
                .hasField("$.errors[?(@.field == 'user_id')].type", firstContains("validation.is_uuid"));

    }

    @Test
    public void promote_failsWhenUserIdDoesNotExists() {

        request.asUser(ADMIN_USER).to("admin/promote")
                .withField("user_id", UUID.randomUUID()).post()
                .isBadRequest()
                .hasField("$.errors[?(@.field == 'user_id')].type", firstContains("validation.do_not_exist"));

    }

    @Test
    public void promote_failsWhenUserTryToUpdateHimself() {

        var userId = request.asUser(ADMIN_USER).to("auth/profile")
                .get().isOk().getBody().get("id").asText();

        request.asUser(ADMIN_USER).to("admin/promote")
                .withField("user_id", userId).post()
                .isBadRequest()
                .hasField("$.errors[?(@.field == 'user_id')].type", firstContains("validation.cannot_change_role_current_user"));

    }

    @Test
    public void promote_successUpdateOtherUserToAdmin() {

        var otherId = request.asUser("another@gmail.com").to("auth/profile")
                .get().isOk()
                .hasField("$.role", contains("ROLE_USER"))
                .getBody().get("id").asText();

        request.asUser(ADMIN_USER).to("admin/promote")
                .withField("user_id", otherId)
                .withField("role", "ROLE_ADMIN")
                .post().isOk();

        request.asUser("another@gmail.com").to("auth/profile")
                .get().isOk()
                .hasField("$.role", contains("ROLE_ADMIN"));

    }

    @Test
    public void statistics_failsWhenUserAccess() {

        request.asUser(DEFAULT_USER).to("admin/statistics")
                .get().isForbidden();

    }

    @Test
    public void statistics_successShowTotals() {

        var tasks = entityRequest.asUser(DEFAULT_USER).makeTasks("Task A", "Task B", "Task C");

        entityRequest.asUser(DEFAULT_USER).commentOnTask(tasks.get(0).uuid(), "Comment A", "Comment B", "Comment C", "Comment D");

        entityRequest.asUser(DEFAULT_USER).makeTags("Tag A", "Tag B");

        entityRequest.asUser(DEFAULT_USER).makeProjects("Project A");

        request.asUser(ADMIN_USER).to("admin/statistics")
                .get().isOk()
                .hasField("$.total_users", isEqualTo(2))
                .hasField("$.total_tasks", isEqualTo(3))
                .hasField("$.total_comments", isEqualTo(4))
                .hasField("$.total_tags", isEqualTo(2))
                .hasField("$.total_projects", isEqualTo(1));

    }

}
