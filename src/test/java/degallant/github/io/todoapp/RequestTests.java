package degallant.github.io.todoapp;

import degallant.github.io.todoapp.test.IntegrationTest;
import org.junit.jupiter.api.Test;

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

}