package degallant.github.io.todoapp;

import degallant.github.io.todoapp.common.IntegrationTest;
import org.junit.jupiter.api.Test;

public class RequestTests extends IntegrationTest {

    @Test
    public void body_failsWhenInvalid() {

        request.asUser(DEFAULT_USER).to("tasks").withBody("").post().isBadRequest();
        request.asUser(DEFAULT_USER).to("tasks").post().isBadRequest();

    }

}