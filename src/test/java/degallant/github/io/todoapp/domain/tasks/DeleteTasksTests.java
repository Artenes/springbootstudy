package degallant.github.io.todoapp.domain.tasks;

import degallant.github.io.todoapp.test.IntegrationTest;
import org.junit.jupiter.api.Test;

import java.util.UUID;

public class DeleteTasksTests extends IntegrationTest {

    @Test
    public void delete_failsWhenIdIsInvalid() {

        request.asUser(DEFAULT_USER).to("tasks/invalid")
                .delete().isNotFound();

    }

    @Test
    public void delete_failsWhenIdDoesNotExists() {

        request.asUser(DEFAULT_USER).to("tasks/" + UUID.randomUUID())
                .delete().isNotFound();

    }

    @Test
    public void delete_failsWhenIdIsFromDeletedTask() {

        var task = entityRequest.asUser(DEFAULT_USER).makeTask("Task A");

        request.asUser(DEFAULT_USER).to(task.uri())
                .delete().isNoContent();

        request.asUser(DEFAULT_USER).to(task.uri())
                .delete().isNotFound();

    }

}
