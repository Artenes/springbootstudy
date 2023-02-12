package degallant.github.io.todoapp.domain.tasks;

import degallant.github.io.todoapp.test.IntegrationTest;
import org.junit.jupiter.api.Test;

import java.util.UUID;

public class TasksDetailsTests extends IntegrationTest {

    @Test
    public void detail_failsWhenIdIsInvalid() {

        request.asUser(DEFAULT_USER).to("tasks/invalid")
                .get().isNotFound();

    }

    @Test
    public void details_failsWhenIdIsNotFound() {

        request.asUser(DEFAULT_USER).to("tasks/" + UUID.randomUUID())
                .get().isNotFound();

    }

    @Test
    public void details_showsSubtasks() {

        var task = entityRequest.asUser(DEFAULT_USER).makeTask("Task A");

        entityRequest.asUser(DEFAULT_USER).makeTaskWithDetails("title", "Subtask A", "parent_id", task.uuid().toString());
        entityRequest.asUser(DEFAULT_USER).makeTaskWithDetails("title", "Subtask B", "parent_id", task.uuid().toString());
        entityRequest.asUser(DEFAULT_USER).makeTaskWithDetails("title", "Subtask C", "parent_id", task.uuid().toString());

        request.asUser(DEFAULT_USER).to(task.uri())
                .get().isOk()
                .hasField("$._embedded.subtasks[?(@.title == 'Subtask A')]", exists())
                .hasField("$._embedded.subtasks[?(@.title == 'Subtask B')]", exists())
                .hasField("$._embedded.subtasks[?(@.title == 'Subtask C')]", exists());

    }

    @Test
    public void user_canSeeOnlyItsTask() {

        var uri = entityRequest.asUser("another@gmail.com").makeTasks("Task A").get(0).uri();

        request.asUser(DEFAULT_USER).to(uri)
                .get().isNotFound();

    }

}
