package degallant.github.io.todoapp.domain.tasks;

import degallant.github.io.todoapp.test.IntegrationTest;
import org.junit.jupiter.api.Test;

import java.util.UUID;

public class TasksDetailsTests extends IntegrationTest {

    @Test
    public void details_failsWhenTaskWasDeleted() {

        var taskId = entityRequest.asUser(DEFAULT_USER).makeTask("Task A");

        request.asUser(DEFAULT_USER).to(taskId.uri())
                .delete().isNoContent();

        request.asUser(DEFAULT_USER).to(taskId.uri())
                .get().isNotFound();

    }

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
    public void details_changeDateTimeDisplay() {

        var dueDate = "2030-01-01T12:50:29.790511-04:00";
        var task = entityRequest.asUser(DEFAULT_USER).makeTaskWithDetails("title", "Task A", "due_date", dueDate);

        request.asUser(DEFAULT_USER).to(task.uri())
                .get().isOk()
                .hasField("$.due_date", isEqualTo(dueDate));

        request.asUser(DEFAULT_USER).to(task.uri())
                .withHeader("Accept-Offset", "+02:00")
                .get().isOk()
                .hasField("$.due_date", isEqualTo("2030-01-01T18:50:29.790511+02:00"));

    }

    @Test
    public void user_canSeeOnlyItsTask() {

        var uri = entityRequest.asUser("another@gmail.com").makeTasks("Task A").get(0).uri();

        request.asUser(DEFAULT_USER).to(uri)
                .get().isNotFound();

    }

}
