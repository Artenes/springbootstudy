package degallant.github.io.todoapp;

import degallant.github.io.todoapp.common.IntegrationTest;
import org.junit.jupiter.api.Test;

import java.util.Map;

/**
 * @noinspection ConstantConditions
 */
public class TasksTests extends IntegrationTest {

    @Test
    public void failsWhenRequestBodyIsInvalid() {

        authenticate();
        client.post().uri("/v1/tasks").bodyValue("").exchange()
                .expectStatus().isBadRequest();

        client.post().uri("/v1/tasks").exchange()
                .expectStatus().isBadRequest();

    }

    @Test
    public void failsToReturnDetailsWhenIdIsInvalid() {

        authenticate();
        client.get().uri("/v1/tasks/invalid").exchange()
                .expectStatus().isNotFound();

    }

    protected void createTasks(String prefix, int amount) {
        for (int count = 1; count <= amount; count++) {
            String task = prefix + " " + count;
            createTasks(task);
        }
    }

    protected void createTasks(String... tasks) {
        for (String task : tasks) {
            client.post().uri("/v1/tasks")
                    .bodyValue(Map.of("title", task))
                    .exchange()
                    .expectStatus().isCreated();
        }
    }

    protected void createTask(String title, boolean complete, String dueDate) {
        client.post().uri("/v1/tasks").bodyValue(Map.of(
                "title", title,
                "complete", complete,
                "due_date", dueDate
        )).exchange().expectStatus().isCreated();
    }

}