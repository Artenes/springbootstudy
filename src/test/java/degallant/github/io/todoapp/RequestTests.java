package degallant.github.io.todoapp;

import degallant.github.io.todoapp.test.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class RequestTests extends IntegrationTest {

    @Test
    public void body_failsWhenInvalid() {

        request.asUser(DEFAULT_USER).to("tasks").withBody("").post().isBadRequest();
        request.asUser(DEFAULT_USER).to("tasks").post().isBadRequest().show();

    }

    @Test
    public void user_cant_access_admin_routes() {

        request.asUser(DEFAULT_USER).to("admin/promote").post().isForbidden();
        request.asUser(DEFAULT_USER).to("admin/statistics").get().isForbidden();
        request.asUser(DEFAULT_USER).to("admin/users/id").delete().isForbidden();
        request.asUser(DEFAULT_USER).to("admin/users/id/restore").patch().isForbidden();

    }

    @Test
    public void all_fails_with_invalid_api_key() {

        request.asUser(DEFAULT_USER).to("auth/profile")
                .withApiKey("invalid").get().isBadRequest()
                .hasField("$.errors[?(@.field == 'Client-Agent')].type", firstContains("validation.do_not_exist"));

        request.asUser(DEFAULT_USER).to("auth/profile")
                .withApiKey(UUID.randomUUID().toString()).get().isBadRequest()
                .hasField("$.errors[?(@.field == 'Client-Agent')].type", firstContains("validation.do_not_exist"));

        //delete api key from database
        var apiKey = apiKeyRepository.findById(this.apiKey).orElseThrow();
        apiKey.setDeletedAt(OffsetDateTime.now());
        apiKeyRepository.save(apiKey);

        try {
            request.asUser(DEFAULT_USER).to("auth/profile")
                    .withApiKey(this.apiKey.toString()).get().isBadRequest()
                    .hasField("$.errors[?(@.field == 'Client-Agent')].type", firstContains("validation.do_not_exist"));
        } catch (AssertionFailedError error) {
            assertTrue(error.getMessage().contains("400 BAD_REQUEST"), "Request passing even with deleted api key");
        }

    }

}