package degallant.github.io.todoapp.admin;

import degallant.github.io.todoapp.test.IntegrationTest;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

/**
 * @noinspection OptionalGetWithoutIsPresent
 */
public class AuthAdminTests extends IntegrationTest {

    @Test
    public void post_fails_authentication_admin_creation() {

        var openIdToken = authenticator.makeOpenIdTokenFor("admin@email.com");
        request.asGuest().to("admin/auth").withField("open_id_token", openIdToken).post().isCreated();

        var anotherOpenIdToken = authenticator.makeOpenIdTokenFor("anotheradmin@email.com");
        request.asGuest().to("admin/auth").withField("open_id_token", anotherOpenIdToken).post().isConflict()
                .hasField("$.type", contains("error.user_cannot_create_another_admin"));

    }

    @Test
    public void post_fails_authentication_deleted_admin() {

        var openIdToken = authenticator.makeOpenIdTokenFor("admin@email.com");
        request.asGuest().to("admin/auth").withField("open_id_token", openIdToken).post().isCreated();

        //TODO decouple repo usage from test
        var user = usersRepository.findByEmail("admin@email.com").get();
        user.setDeletedAt(OffsetDateTime.now());
        usersRepository.save(user);

        request.asGuest().to("admin/auth").withField("open_id_token", openIdToken).post().isConflict()
                .hasField("$.type", contains("error.user_deleted"));

    }

    @Test
    public void post_fails_authentication_not_admin() {

        var openIdToken = authenticator.makeOpenIdTokenFor(DEFAULT_USER);
        request.asGuest().to("auth").withField("open_id_token", openIdToken).post().isCreated();

        request.asGuest().to("admin/auth").withField("open_id_token", openIdToken).post().isConflict()
                .hasField("$.type", contains("error.user_invalid_role"));

    }

    @Test
    public void post_fails_authentication_invalid_body() {

        request.asGuest().to("admin/auth")
                .withField("random", "value").post().isBadRequest()
                .hasField("$.errors[0].type", contains("validation.is_required"));

        request.asGuest().to("admin/auth")
                .withField("open_id_token", "").post().isBadRequest()
                .hasField("$.errors[0].type", contains("validation.is_empty"));

        authenticator.makeParsingFailsTo("invalid");
        request.asGuest().to("admin/auth")
                .withField("open_id_token", "invalid").post().isBadRequest()
                .hasField("$.errors[0].type", contains("validation.openid_extraction_failed"));

    }

    @Test
    public void post_success_create_user() {

        var openIdToken = authenticator.makeOpenIdTokenFor("admin@email.com");
        request.asGuest().to("admin/auth").withField("open_id_token", openIdToken).post().isCreated()
                .hasField("$.access_token", exists())
                .hasField("$.refresh_token", exists());

    }

    @Test
    public void post_success_auth_user() {

        var openIdToken = authenticator.makeOpenIdTokenFor("admin@email.com");
        request.asGuest().to("admin/auth").withField("open_id_token", openIdToken).post().isCreated();
        request.asGuest().to("admin/auth").withField("open_id_token", openIdToken).post().isOk()
                .hasField("$.access_token", exists())
                .hasField("$.refresh_token", exists());

    }

}
