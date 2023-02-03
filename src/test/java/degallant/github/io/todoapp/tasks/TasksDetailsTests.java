package degallant.github.io.todoapp.tasks;

import degallant.github.io.todoapp.test.IntegrationTest;
import org.junit.jupiter.api.Test;

import java.util.UUID;

public class TasksDetailsTests extends IntegrationTest {

    @Test
    public void taskDetails_failsWhenIdIsInvalid() {

        request.asUser(DEFAULT_USER).to("tasks/invalid")
                .get().isNotFound();

    }

    @Test
    public void taskDetails_failsWhenIdIsNotFound() {

        request.asUser(DEFAULT_USER).to("tasks/" + UUID.randomUUID())
                .get().isNotFound();

    }

    @Test
    public void user_canSeeOnlyItsTask() {

        var uri = entityRequest.asUser("another@gmail.com").makeTasks("Task A").get(0).uri();

        request.asUser(DEFAULT_USER).to(uri)
                .get().isNotFound();

    }

}
