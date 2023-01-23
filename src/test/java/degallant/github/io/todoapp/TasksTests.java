package degallant.github.io.todoapp;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.List;
import java.util.Map;

/**
 * @noinspection ConstantConditions
 */
public class TasksTests extends IntegrationTest {

    @Test
    public void taskCannotBeCreatedWithInvalidData() {
        authenticate();
        client.post().uri("/v1/tasks").bodyValue(Map.of(
                "title", ""
        )).exchange().expectStatus().isBadRequest();

        client.post().uri("/v1/tasks").bodyValue(Map.of(
                "title", "Title",
                "description", ""
        )).exchange().expectStatus().isBadRequest();

        client.post().uri("/v1/tasks").bodyValue(Map.of(
                "title", "Title",
                "due_date", "invalid"
        )).exchange().expectStatus().isBadRequest().expectBody().consumeWith(System.out::println);

        client.post().uri("/v1/tasks").bodyValue(Map.of(
                "title", "Title",
                "priority", "invalid"
        )).exchange().expectStatus().isBadRequest();

        client.post().uri("/v1/tasks").bodyValue(Map.of(
                "title", "Title",
                "tags_ids", "invalid"
        )).exchange().expectStatus().isBadRequest();

        client.post().uri("/v1/tasks").bodyValue(Map.of(
                "title", "Title",
                "parent_id", "invalid"
        )).exchange().expectStatus().isBadRequest();

        client.post().uri("/v1/tasks").bodyValue(Map.of(
                "title", "Title",
                "project_id", "invalid"
        )).exchange().expectStatus().isBadRequest();
    }

    @Test
    public void oneUserCantSeeOthersUsersTasks() {

        String taskFromUserA = "Take dog for a walk";
        String taskFromUserB = "Take cat for a walk";

        //create a task for user A
        authenticate("usera@gmail.com");
        URI taskFromUserAUri = client.post().uri("/v1/tasks")
                .bodyValue(Map.of("title", taskFromUserA))
                .exchange()
                .expectStatus().isCreated()
                .expectBody().returnResult()
                .getResponseHeaders().getLocation();

        //create a task for user B
        authenticate("userb@gmail.com");
        URI taskFromUserBUri = client.post().uri("/v1/tasks")
                .bodyValue(Map.of("title", taskFromUserB))
                .exchange()
                .expectStatus().isCreated()
                .expectBody().returnResult()
                .getResponseHeaders().getLocation();

        //check user A created task and try to see created task from user b
        authenticate("usera@gmail.com");
        client.get().uri(taskFromUserAUri)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.title").isEqualTo(taskFromUserA);
        client.get().uri(taskFromUserBUri)
                .exchange()
                .expectStatus().isNotFound();

        //check user B created task and try to see created task from user a
        authenticate("userb@gmail.com");
        client.get().uri(taskFromUserBUri)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.title").isEqualTo(taskFromUserB);

        client.get().uri(taskFromUserAUri)
                .exchange()
                .expectStatus().isNotFound();

    }

    @Test
    public void oneUserCanEditOnlyItsTasks() {

        String taskFromUserA = "Take dog for a walk";
        String taskFromUserB = "Take cat for a walk";

        //create a task for user A
        authenticate("usera@gmail.com");
        URI taskFromUserAUri = client.post().uri("/v1/tasks")
                .bodyValue(Map.of("title", taskFromUserA))
                .exchange()
                .expectStatus().isCreated()
                .expectBody().returnResult()
                .getResponseHeaders().getLocation();

        //create a task for user B
        authenticate("userb@gmail.com");
        URI taskFromUserBUri = client.post().uri("/v1/tasks")
                .bodyValue(Map.of("title", taskFromUserB))
                .exchange()
                .expectStatus().isCreated()
                .expectBody().returnResult()
                .getResponseHeaders().getLocation();

        //confirm that user A can edit its task, while not being able to edits B's
        authenticate("usera@gmail.com");
        client.patch().uri(taskFromUserAUri)
                .bodyValue(Map.of("complete", true))
                .exchange()
                .expectStatus().isOk();
        client.patch().uri(taskFromUserBUri)
                .bodyValue(Map.of("complete", true))
                .exchange()
                .expectStatus().isNotFound();

        //confirm that user B can edit its task, while not being able to edits A's
        authenticate("userb@gmail.com");
        client.patch().uri(taskFromUserBUri)
                .bodyValue(Map.of("complete", true))
                .exchange()
                .expectStatus().isOk();

        client.patch().uri(taskFromUserAUri)
                .bodyValue(Map.of("complete", true))
                .exchange()
                .expectStatus().isNotFound();

    }

    @Test
    public void aUserCanOnlyListItsOwnTasks() {

        List<String> userATasks = List.of("Take dog for a walk", "Go get milk", "Study for test");
        List<String> userBTasks = List.of("Take cat for a walk", "Play games");

        authenticate("usera@gmail.com");
        createTasksForUser(userATasks);
        authenticate("userb@gmail.com");
        createTasksForUser(userBTasks);

        authenticate("usera@gmail.com");
        client.get().uri("/v1/tasks").exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$._embedded.tasks.length()").isEqualTo(3)
                .jsonPath("$._embedded.tasks.[?(@.title == '%s')]", userATasks.get(0)).exists()
                .jsonPath("$._embedded.tasks.[?(@.title == '%s')]", userATasks.get(1)).exists()
                .jsonPath("$._embedded.tasks.[?(@.title == '%s')]", userATasks.get(2)).exists()
                .jsonPath("$._embedded.tasks.[?(@.title == '%s')]", userBTasks.get(0)).doesNotExist();

        authenticate("userb@gmail.com");
        client.get().uri("/v1/tasks").exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$._embedded.tasks.length()").isEqualTo(2)
                .jsonPath("$._embedded.tasks[?(@.title == '%s')]", userBTasks.get(0)).exists()
                .jsonPath("$._embedded.tasks[?(@.title == '%s')]", userBTasks.get(1)).exists()
                .jsonPath("$._embedded.tasks[?(@.title == '%s')]", userATasks.get(0)).doesNotExist();

    }

    @Test
    public void createsATaskToComplete() {

        authenticate();

        var title = "Take the dog for a walk";

        URI taskUri = client.post().uri("/v1/tasks")
                .bodyValue(Map.of("title", title))
                .exchange()
                .expectStatus().isCreated()
                .expectBody().returnResult()
                .getResponseHeaders().getLocation();

        client.get().uri(taskUri)
                .exchange()
                .expectBody()
                .jsonPath("$.title").isEqualTo(title)
                .jsonPath("$.complete").isEqualTo(false);

    }

    @Test
    public void createsAndCompleteATask() {

        authenticate();

        var title = "Take the dog for a walk";

        URI taskURI = client.post().uri("/v1/tasks")
                .bodyValue(Map.of("title", title))
                .exchange()
                .expectBody()
                .returnResult()
                .getResponseHeaders()
                .getLocation();

        client.patch().uri(taskURI)
                .bodyValue(Map.of("complete", true))
                .exchange();

        show();

        client.get().uri(taskURI)
                .exchange()
                .expectBody()
                .jsonPath("$.title").isEqualTo(title)
                .jsonPath("$.complete").isEqualTo(true);

    }

    @Test
    public void createATaskFullOfDetails() {

        authenticate();

        var title = "Take the dog for a walk";
        var description = "This is very important, dog needs to walk or it will not behave";
        var dueDate = "2030-01-01T12:50:29.790511-04:00";
        var priority = "P3";
        var tags = makeTags("daily", "home", "pet");
        var projectId = makeProject("daily tasks");

        URI taskURI = client.post().uri("/v1/tasks")
                .bodyValue(Map.of(
                        "title", title,
                        "description", description,
                        "due_date", dueDate,
                        "priority", priority,
                        "tags_ids", tags,
                        "project_id", projectId
                ))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .returnResult()
                .getResponseHeaders()
                .getLocation();

        client.get().uri(taskURI)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.title").isEqualTo(title)
                .jsonPath("$.description").isEqualTo(description)
                .jsonPath("$.due_date").isEqualTo(dueDate)
                .jsonPath("$.priority").isEqualTo(priority)
                .jsonPath("$._embedded.project.id").isEqualTo(projectId)
                .jsonPath("$._embedded.tags[?(@.name == '%s')]", "daily").exists()
                .jsonPath("$._embedded.tags[?(@.name == '%s')]", "home").exists()
                .jsonPath("$._embedded.tags[?(@.name == '%s')]", "pet").exists()
                .jsonPath("$._embedded.subtasks").doesNotExist()
                .jsonPath("$._embedded.parent").doesNotExist();

    }

    @Test
    public void createASubTask() {

        authenticate();

        var taskTitle = "Buy some bread";
        URI taskUri = client.post().uri("/v1/tasks")
                .bodyValue(Map.of("title", taskTitle))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .returnResult()
                .getResponseHeaders()
                .getLocation();

        String[] parts = taskUri.getPath().split("/");
        String parentUuid = parts[parts.length - 1];

        var subtaskTitle = "Go to store";
        URI subTaskUri = client.post().uri("/v1/tasks")
                .bodyValue(Map.of(
                        "title", subtaskTitle,
                        "parent_id", parentUuid
                ))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .returnResult()
                .getResponseHeaders()
                .getLocation();

        client.get().uri(taskUri)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.title").isEqualTo(taskTitle)
                .jsonPath("$._embedded.subtasks[0].title").isEqualTo(subtaskTitle)
                .jsonPath("$._embedded.subtasks[0]._links.self.href").isEqualTo(subTaskUri.toString());

        client.get().uri(subTaskUri)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.title").isEqualTo(subtaskTitle)
                .jsonPath("$._embedded.parent._links.self.href").isEqualTo(taskUri.toString())
                .jsonPath("$._embedded.parent.title").isEqualTo(taskTitle);

    }

}