package degallant.github.io.todoapp;

import degallant.github.io.todoapp.test.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

public class AdminTests extends IntegrationTest {

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
        makeAdmin();
    }

    @Test
    public void delete_fails_delete_user_resource_access() {

        request.asUser(ADMIN_USER).to("admin/users/invalid").delete().isNotFound();
        request.asUser(ADMIN_USER).to("admin/users/" + UUID.randomUUID()).delete().isNotFound();

        //cannot delete itself
        var response = request.asUser(ADMIN_USER).to("auth/profile").get().getBody();
        var id = response.get("id").asText();
        request.asUser(ADMIN_USER).to("admin/users/" + id)
                .delete().isConflict().hasField("$.type", contains("error.cannot_delete_current_user"));

        //cannot delete user twice
        var userResponse = request.asUser(DEFAULT_USER).to("auth/profile").get().getBody();
        var userId = userResponse.get("id").asText();
        request.asUser(ADMIN_USER).to("admin/users/" + userId).delete().isNoContent();
        request.asUser(ADMIN_USER).to("admin/users/" + userId).delete().isNotFound();

    }

    @Test
    public void patch_fails_restore_user_resource_access() {

        request.asUser(ADMIN_USER).to("admin/users/invalid/restore").patch().isNotFound();
        request.asUser(ADMIN_USER).to("admin/users/" + UUID.randomUUID() + "/restore").patch().isNotFound();

        //cannot restore user that was not deleted
        var userResponse = request.asUser(DEFAULT_USER).to("auth/profile").get().getBody();
        var userId = userResponse.get("id").asText();
        request.asUser(ADMIN_USER).to("admin/users/" + userId + "/restore")
                .patch().isConflict().hasField("$.type", contains("error.cannot_restore_not_deleted_user"));

        //cannot restore itself
        var response = request.asUser(ADMIN_USER).to("auth/profile").get().getBody();
        var id = response.get("id").asText();
        request.asUser(ADMIN_USER).to("admin/users/" + id + "/restore")
                .patch().isConflict().hasField("$.type", contains("error.cannot_restore_current_user"));

    }

    @Test
    public void path_success_restore_user() {

        var response = request.asUser(DEFAULT_USER).to("auth/profile").get().getBody();
        var id = response.get("id").asText();

        request.asUser(ADMIN_USER).to("admin/users/" + id).delete().isNoContent();
        request.asUser(ADMIN_USER).to("admin/users/" + id + "/restore").patch().isOk();
        request.asUser(DEFAULT_USER).to("auth/profile").get().isOk();

    }

}
