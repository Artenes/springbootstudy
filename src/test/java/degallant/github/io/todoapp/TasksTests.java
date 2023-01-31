package degallant.github.io.todoapp;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;

/**
 * @noinspection ConstantConditions
 */
public class TasksTests extends IntegrationTest {

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
    public void aUserCanOnlyListItsOwnTasks() {

        String[] userATasks = new String[]{"Take dog for a walk", "Go get milk", "Study for test"};
        String[] userBTasks = new String[]{"Take cat for a walk", "Play games"};

        authenticate("usera@gmail.com");
        createTasks(userATasks);
        authenticate("userb@gmail.com");
        createTasks(userBTasks);

        authenticate("usera@gmail.com");
        client.get().uri("/v1/tasks").exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$._embedded.tasks.length()").isEqualTo(3)
                .jsonPath("$._embedded.tasks.[?(@.title == '%s')]", userATasks[0]).exists()
                .jsonPath("$._embedded.tasks.[?(@.title == '%s')]", userATasks[1]).exists()
                .jsonPath("$._embedded.tasks.[?(@.title == '%s')]", userATasks[2]).exists()
                .jsonPath("$._embedded.tasks.[?(@.title == '%s')]", userBTasks[0]).doesNotExist();

        authenticate("userb@gmail.com");
        client.get().uri("/v1/tasks").exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$._embedded.tasks.length()").isEqualTo(2)
                .jsonPath("$._embedded.tasks[?(@.title == '%s')]", userBTasks[0]).exists()
                .jsonPath("$._embedded.tasks[?(@.title == '%s')]", userBTasks[1]).exists()
                .jsonPath("$._embedded.tasks[?(@.title == '%s')]", userATasks[0]).doesNotExist();

    }

    @Test
    public void createATaskFullOfDetails() {

        authenticate();

        var title = "Take the dog for a walk";
        var description = "This is very important, dog needs to walk or it will not behave";
        var dueDate = "2030-01-01T12:50:29.790511-04:00";
        var priority = "P3";
        var tags = makeTagsAsUser(DEFAULT_USER, "daily", "home", "pet");
        var projectId = makeProjectAsUser(DEFAULT_USER, "daily tasks");

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

    @Test
    public void listTasksWithPaginationInformation() {
        authenticate();
        createTasks("Task", 15);

        var responseInBytes = client.get().uri("/v1/tasks")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .consumeWith(System.out::println)
                .jsonPath("$._embedded.tasks.length()").isEqualTo(10)
                .jsonPath("$.count").isEqualTo(10)
                .jsonPath("$.pages").isEqualTo(2)
                .jsonPath("$.total").isEqualTo(15)
                .jsonPath("$._links.next").exists()
                .jsonPath("$._links.previous").doesNotExist()
                .jsonPath("$._links.first").exists()
                .jsonPath("$._links.last").exists()
                .returnResult().getResponseBodyContent();

        JsonNode response = parseResponse(responseInBytes);
        String url = response.get("_links").get("next").get("href").asText();
        var nextURI = URI.create(url);

        client.get().uri(nextURI)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$._embedded.tasks.length()").isEqualTo(5)
                .jsonPath("$.count").isEqualTo(5)
                .jsonPath("$.pages").isEqualTo(2)
                .jsonPath("$.total").isEqualTo(15)
                .jsonPath("$._links.next").doesNotExist()
                .jsonPath("$._links.previous").exists()
                .jsonPath("$._links.first").exists()
                .jsonPath("$._links.last").exists();
    }

    @Test
    public void listPaginationInformationWithoutTasks() {

        authenticate();
        client.get().uri("/v1/tasks")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$._embedded.tasks.length()").isEqualTo(0)
                .jsonPath("$.count").isEqualTo(0)
                .jsonPath("$.pages").isEqualTo(0)
                .jsonPath("$.total").isEqualTo(0)
                .jsonPath("$._links.next").doesNotExist()
                .jsonPath("$._links.previous").doesNotExist()
                .jsonPath("$._links.first").doesNotExist()
                .jsonPath("$._links.last").doesNotExist();

    }

    @Test
    public void sortTaskList() {

        authenticate();

        createTasks("Task B", "Task D", "Task C", "Task A");
        client.get().uri("/v1/tasks?s=title:asc").exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$._embedded.tasks[0].title").isEqualTo("Task A")
                .jsonPath("$._embedded.tasks[1].title").isEqualTo("Task B")
                .jsonPath("$._embedded.tasks[2].title").isEqualTo("Task C")
                .jsonPath("$._embedded.tasks[3].title").isEqualTo("Task D");

        client.get().uri("/v1/tasks?s=title:desc").exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$._embedded.tasks[0].title").isEqualTo("Task D")
                .jsonPath("$._embedded.tasks[1].title").isEqualTo("Task C")
                .jsonPath("$._embedded.tasks[2].title").isEqualTo("Task B")
                .jsonPath("$._embedded.tasks[3].title").isEqualTo("Task A");

        client.get().uri("/v1/tasks").exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$._embedded.tasks[0].title").isEqualTo("Task B")
                .jsonPath("$._embedded.tasks[1].title").isEqualTo("Task D")
                .jsonPath("$._embedded.tasks[2].title").isEqualTo("Task C")
                .jsonPath("$._embedded.tasks[3].title").isEqualTo("Task A");

    }

    @Test
    public void sortTaskByDueDate() {

        authenticate();

        client.post().uri("/v1/tasks")
                .bodyValue(Map.of("title", "Task A", "due_date", "2030-01-01T12:50:29.790511-04:00"))
                .exchange()
                .expectStatus().isCreated();

        client.post().uri("/v1/tasks")
                .bodyValue(Map.of("title", "Task B", "due_date", "2030-02-01T12:50:29.790511-04:00"))
                .exchange()
                .expectStatus().isCreated();

        client.get().uri("/v1/tasks?s=due_date:asc").exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$._embedded.tasks[0].title").isEqualTo("Task A")
                .jsonPath("$._embedded.tasks[1].title").isEqualTo("Task B");

        client.get().uri("/v1/tasks?s=due_date:desc").exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$._embedded.tasks[0].title").isEqualTo("Task B")
                .jsonPath("$._embedded.tasks[1].title").isEqualTo("Task A");

    }

    @Test
    public void failsWithInvalidSortQuery() {

        authenticate();

        client.get().uri("/v1/tasks?s=title:invalid").exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.errors[0].type").value(containsString("error.invalid_sort_direction.detail"));

        client.get().uri("/v1/tasks?s=invalid:desc").exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.errors[0].type").value(containsString("error.invalid_sort_attribute.detail"));

        client.get().uri("/v1/tasks?s=title:desc,due_date").exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.errors[0].type").value(containsString("error.invalid_sort_query.detail"));

    }

    @Test
    public void failsWithInvalidQueryParams() {
        authenticate();
        client.get().uri("/v1/tasks?p=invalid").exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.errors[0].field").isEqualTo("p");

        client.get().uri("/v1/tasks?s=").exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.errors[0].field").isEqualTo("s");

        client.get().uri("/v1/tasks?title=").exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.errors[0].field").isEqualTo("title");

        client.get().uri("/v1/tasks?due_date=invalid").exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.errors[0].field").isEqualTo("due_date");

        client.get().uri("/v1/tasks?complete=invalid").exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.errors[0].field").isEqualTo("complete");
    }

    @Test
    public void filterTaskList() {

        authenticate();

        createTask("Take dog for walk", false, "2030-01-01T12:50:29.790511-04:00");
        createTask("Go shopping", true, "2030-01-02T12:50:29.790511-04:00");
        createTask("Clean room", true, "2030-01-04T12:50:29.790511-04:00");
        createTask("Paint wall", false, "2030-01-04T12:50:29.790511-04:00");

        client.get().uri("/v1/tasks?title=shopping").exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$._embedded.tasks.length()").isEqualTo(1)
                .jsonPath("$._embedded.tasks[0].title").isEqualTo("Go shopping");

        client.get().uri("/v1/tasks?complete=false").exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$._embedded.tasks.length()").isEqualTo(2)
                .jsonPath("$._embedded.tasks[0].title").isEqualTo("Take dog for walk")
                .jsonPath("$._embedded.tasks[1].title").isEqualTo("Paint wall");

        client.get().uri("/v1/tasks?due_date=2030-01-04").exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$._embedded.tasks.length()").isEqualTo(2)
                .jsonPath("$._embedded.tasks[0].title").isEqualTo("Clean room")
                .jsonPath("$._embedded.tasks[1].title").isEqualTo("Paint wall");

    }

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