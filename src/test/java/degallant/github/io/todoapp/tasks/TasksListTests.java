package degallant.github.io.todoapp.tasks;

import degallant.github.io.todoapp.common.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.reactive.server.JsonPathAssertions;

import static org.hamcrest.Matchers.containsString;

public class TasksListTests extends IntegrationTest {

    @Test
    public void user_canListOnlyItsTasks() {

        entityRequest.asUser(DEFAULT_USER).makeTasks("Task A", "Task B", "Task C");
        entityRequest.asUser("another@gmail.com").makeTasks("Task D", "Task E");

        request.asUser(DEFAULT_USER).to("tasks")
                .get().isOk()
                .hasField("$._embedded.tasks.length()", v -> v.isEqualTo(3))
                .hasField("$._embedded.tasks.[?(@.title == 'Task A')]", JsonPathAssertions::exists)
                .hasField("$._embedded.tasks.[?(@.title == 'Task B')]", JsonPathAssertions::exists)
                .hasField("$._embedded.tasks.[?(@.title == 'Task C')]", JsonPathAssertions::exists);

    }

    @Test
    public void list_containsTaskDetails() {

        entityRequest.asUser(DEFAULT_USER).makeTask(
                "title", "Task A",
                "description", "task description",
                "due_date", "2030-01-01T12:50:29.790511-04:00"
        );

        request.asUser(DEFAULT_USER).to("tasks").get().isOk()
                .hasField("$._embedded.tasks.length()", JsonPathAssertions::exists)
                .hasField("$._embedded.tasks[0].title", v -> v.isEqualTo("Task A"))
                .hasField("$._embedded.tasks[0].description", v -> v.isEqualTo("task description"))
                .hasField("$._embedded.tasks[0].due_date", v -> v.isEqualTo("2030-01-01T12:50:29.790511-04:00"))
                .hasField("$._embedded.tasks[0].complete", v -> v.isEqualTo(false));

    }

    @Test
    public void pagination_hasPageInformation() {

        entityRequest.asUser(DEFAULT_USER).makeTasks("Task A", "Task B");

        request.asUser(DEFAULT_USER).to("tasks").get().isOk()
                .hasField("$.count", v -> v.isEqualTo(2))
                .hasField("$.pages", v -> v.isEqualTo(1))
                .hasField("$.total", v -> v.isEqualTo(2))
                .show();

    }

    @Test
    public void pagination_hasPageInformationWithoutTasks() {

        request.asUser(DEFAULT_USER).to("tasks").get().isOk()
                .hasField("$._embedded.tasks.length()", v -> v.isEqualTo(0))
                .hasField("$.count", v -> v.isEqualTo(0))
                .hasField("$.pages", v -> v.isEqualTo(0))
                .hasField("$.total", v -> v.isEqualTo(0))
                .hasField("$._links.next", JsonPathAssertions::doesNotExist)
                .hasField("$._links.previous", JsonPathAssertions::doesNotExist)
                .hasField("$._links.first", JsonPathAssertions::doesNotExist)
                .hasField("$._links.last", JsonPathAssertions::doesNotExist);

    }

    @Test
    public void pagination_canNavigateToNextPage() {

        entityRequest.asUser(DEFAULT_USER).makeNTasks(15);

        var next = request.asUser(DEFAULT_USER).to("tasks").get().isOk()
                .hasField("$.count", v -> v.isEqualTo(10))
                .getBody().get("_links").get("next").get("href").asText();

        request.asUser(DEFAULT_USER).to(next).get().isOk()
                .hasField("$.count", v -> v.isEqualTo(5));

    }

    @Test
    public void pagination_canNavigateToPreviousPage() {

        entityRequest.asUser(DEFAULT_USER).makeNTasks(15);

        var previous = request.asUser(DEFAULT_USER).to("tasks")
                .withParam("p", 2).get().isOk()
                .hasField("$.count", v -> v.isEqualTo(5))
                .getBody().get("_links").get("previous").get("href").asText();

        request.asUser(DEFAULT_USER).to(previous).get().isOk()
                .hasField("$.count", v -> v.isEqualTo(10))
                .getBody().get("_links").get("last").get("href").asText();

    }

    @Test
    public void pagination_canNavigateToLastPage() {

        entityRequest.asUser(DEFAULT_USER).makeNTasks(15);

        var last = request.asUser(DEFAULT_USER).to("tasks").get().isOk()
                .hasField("$.count", v -> v.isEqualTo(10))
                .getBody().get("_links").get("last").get("href").asText();

        request.asUser(DEFAULT_USER).to(last).get().isOk()
                .hasField("$.count", v -> v.isEqualTo(5));

    }

    @Test
    public void pagination_canNavigateToFirstPage() {

        entityRequest.asUser(DEFAULT_USER).makeNTasks(15);

        var first = request.asUser(DEFAULT_USER).to("tasks")
                .withParam("p", 2).get().isOk()
                .hasField("$.count", v -> v.isEqualTo(5))
                .getBody().get("_links").get("first").get("href").asText();

        request.asUser(DEFAULT_USER).to(first).get().isOk()
                .hasField("$.count", v -> v.isEqualTo(10));

    }

    @Test
    public void pagination_failsWhenInvalidPageIsUsed() {

        request.asUser(DEFAULT_USER).to("tasks")
                .withParam("p", "invalid").get().isBadRequest()
                .hasField("$.errors[0].field", v -> v.isEqualTo("p"))
                .hasField("$.errors[0].type", v -> v.value(containsString("validation.is_not_a_number")));

    }

    @Test
    public void pagination_worksWhenBlankPageIsUsed() {

        request.asUser(DEFAULT_USER).to("tasks").withParam("p", "").get().isOk();

    }

    @Test
    public void pagination_failsWhenNegativePageIsUsed() {

        request.asUser(DEFAULT_USER).to("tasks")
                .withParam("p", "-3").get().isBadRequest()
                .hasField("$.errors[0].field", v -> v.isEqualTo("p"))
                .hasField("$.errors[0].type", v -> v.value(containsString("validation.is_positive")));

    }

    @Test
    public void pagination_returnsNothingWhenWrongPageIsUsed() {

        entityRequest.asUser(DEFAULT_USER).makeNTasks(15);

        request.asUser(DEFAULT_USER).to("tasks")
                .withParam("p", 39).get().isOk()
                .hasField("$.count", v -> v.isEqualTo(0))
                .show();

    }

    @Test
    public void sorting_failsWithInvalidSort() {

        request.asUser(DEFAULT_USER).to("tasks").withParam("s", "title:invalid")
                .get().isBadRequest()
                .hasField("$.errors[0].type", v -> v.value(containsString("error.invalid_sort_direction.detail")));

    }

    @Test
    public void sorting_failsWithInvalidField() {

        request.asUser(DEFAULT_USER).to("tasks").withParam("s", "invalid:asc")
                .get().isBadRequest()
                .hasField("$.errors[0].type", v -> v.value(containsString("error.invalid_sort_attribute.detail")));

    }

    @Test
    public void sorting_failsWithInvalidQuery() {

        request.asUser(DEFAULT_USER).to("tasks").withParam("s", "title:asc,due_date")
                .get().isBadRequest()
                .hasField("$.errors[0].type", v -> v.value(containsString("error.invalid_sort_query.detail")));

    }

    @Test
    public void sorting_failsWithEmptyQuery() {

        request.asUser(DEFAULT_USER).to("tasks").withParam("s", "")
                .get().isBadRequest()
                .hasField("$.errors[0].type", v -> v.value(containsString("error.invalid_sort_query.detail")));

    }

    @Test
    public void sorting_sortByTitleAsc() {

        entityRequest.asUser(DEFAULT_USER).makeTasks("Task B", "Task D", "Task C", "Task A");

        request.asUser(DEFAULT_USER).to("tasks").withParam("s", "title:asc")
                .get().isOk()
                .hasField("$._embedded.tasks[0].title", v -> v.isEqualTo("Task A"))
                .hasField("$._embedded.tasks[1].title", v -> v.isEqualTo("Task B"))
                .hasField("$._embedded.tasks[2].title", v -> v.isEqualTo("Task C"))
                .hasField("$._embedded.tasks[3].title", v -> v.isEqualTo("Task D"));

    }

    @Test
    public void sorting_sortByTitleDesc() {

        entityRequest.asUser(DEFAULT_USER).makeTasks("Task B", "Task D", "Task C", "Task A");

        request.asUser(DEFAULT_USER).to("tasks").withParam("s", "title:desc")
                .get().isOk()
                .hasField("$._embedded.tasks[0].title", v -> v.isEqualTo("Task D"))
                .hasField("$._embedded.tasks[1].title", v -> v.isEqualTo("Task C"))
                .hasField("$._embedded.tasks[2].title", v -> v.isEqualTo("Task B"))
                .hasField("$._embedded.tasks[3].title", v -> v.isEqualTo("Task A"));

    }

    @Test
    public void sorting_sortByDueDateAsc() {

        entityRequest.asUser(DEFAULT_USER).makeTask("title", "Task A", "due_date", "2030-01-01T12:50:29.790511-04:00");
        entityRequest.asUser(DEFAULT_USER).makeTask("title", "Task B", "due_date", "2030-02-01T12:50:29.790511-04:00");

        request.asUser(DEFAULT_USER).to("tasks").withParam("s", "due_date:asc")
                .get().isOk()
                .hasField("$._embedded.tasks[0].title", v -> v.isEqualTo("Task A"))
                .hasField("$._embedded.tasks[1].title", v -> v.isEqualTo("Task B"));

    }

    @Test
    public void sorting_sortByDueDateDesc() {

        entityRequest.asUser(DEFAULT_USER).makeTask("title", "Task A", "due_date", "2030-01-01T12:50:29.790511-04:00");
        entityRequest.asUser(DEFAULT_USER).makeTask("title", "Task B", "due_date", "2030-02-01T12:50:29.790511-04:00");

        request.asUser(DEFAULT_USER).to("tasks").withParam("s", "due_date:desc")
                .get().isOk()
                .hasField("$._embedded.tasks[0].title", v -> v.isEqualTo("Task B"))
                .hasField("$._embedded.tasks[1].title", v -> v.isEqualTo("Task A"));

    }

    @Test
    public void sorting_sortByMoreThanOneField() {

        entityRequest.asUser(DEFAULT_USER).makeTask("title", "Task A", "due_date", "2030-01-01T12:50:29.790511-04:00");
        entityRequest.asUser(DEFAULT_USER).makeTask("title", "Task B", "due_date", "2030-02-01T12:50:29.790511-04:00");

        request.asUser(DEFAULT_USER).to("tasks").withParam("s", "due_date:asc,title:desc")
                .get().isOk()
                .hasField("$._embedded.tasks[0].title", v -> v.isEqualTo("Task A"))
                .hasField("$._embedded.tasks[1].title", v -> v.isEqualTo("Task B"));

    }

    @Test
    public void filter_byTitle() {

        entityRequest.asUser(DEFAULT_USER).makeTasks("Tak cat to vet", "Take dog for walk");

        request.asUser(DEFAULT_USER).to("tasks").withParam("title", "dog")
                .get().isOk()
                .hasField("$._embedded.tasks.length()", v -> v.isEqualTo(1))
                .hasField("$._embedded.tasks[0].title", v -> v.isEqualTo("Take dog for walk"));

    }

    @Test
    public void filter_failsWithEmptyTitle() {

        request.asUser(DEFAULT_USER).to("tasks").withParam("title", "")
                .get().isBadRequest()
                .hasField("$.errors[0].type", v -> v.value(containsString("validation.is_empty")));

    }

    @Test
    public void filter_byCompleteTasks() {

        entityRequest.asUser(DEFAULT_USER).makeTask("title", "Take dog for a walk", "complete", "true");
        entityRequest.asUser(DEFAULT_USER).makeTask("title", "Take cat to vet", "complete", "false");

        request.asUser(DEFAULT_USER).to("tasks").withParam("complete", "true")
                .get().isOk()
                .hasField("$._embedded.tasks.length()", v -> v.isEqualTo(1))
                .hasField("$._embedded.tasks[0].title", v -> v.isEqualTo("Take dog for a walk"));

    }

    @Test
    public void filter_byNotCompletedTasks() {

        entityRequest.asUser(DEFAULT_USER).makeTask("title", "Take dog for a walk", "complete", "true");
        entityRequest.asUser(DEFAULT_USER).makeTask("title", "Take cat to vet", "complete", "false");

        request.asUser(DEFAULT_USER).to("tasks").withParam("complete", "false")
                .get().isOk()
                .hasField("$._embedded.tasks.length()", v -> v.isEqualTo(1))
                .hasField("$._embedded.tasks[0].title", v -> v.isEqualTo("Take cat to vet"));

    }

    @Test
    public void filter_failsWithInvalidComplete() {

        request.asUser(DEFAULT_USER).to("tasks").withParam("complete", "invalid")
                .get().isBadRequest()
                .hasField("$.errors[0].type", v -> v.value(containsString("validation.is_boolean")));

    }

    @Test
    public void filter_failsWithEmptyComplete() {

        request.asUser(DEFAULT_USER).to("tasks").withParam("complete", "")
                .get().isBadRequest()
                .hasField("$.errors[0].type", v -> v.value(containsString("validation.is_boolean")));

    }

    @Test
    public void filter_byDate() {

        entityRequest.asUser(DEFAULT_USER).makeTask("title", "Take dog for a walk", "due_date", "2030-01-01T12:50:29.790511-04:00");
        entityRequest.asUser(DEFAULT_USER).makeTask("title", "Take cat to vet", "due_date", "2030-01-03T12:50:29.790511-04:00");

        request.asUser(DEFAULT_USER).to("tasks").withParam("due_date", "2030-01-03")
                .get().isOk()
                .hasField("$._embedded.tasks.length()", v -> v.isEqualTo(1))
                .hasField("$._embedded.tasks[0].title", v -> v.isEqualTo("Take cat to vet"));

    }

    @Test
    public void filter_failsWithInvalidDate() {

        request.asUser(DEFAULT_USER).to("tasks").withParam("due_date", "invalid")
                .get().isBadRequest()
                .hasField("$.errors[0].type", v -> v.value(containsString("validation.is_date")));

    }

    @Test
    public void filter_failsWithEmptyDate() {

        request.asUser(DEFAULT_USER).to("tasks").withParam("due_date", "")
                .get().isBadRequest()
                .hasField("$.errors[0].type", v -> v.value(containsString("validation.is_date")));

    }

    @Test
    public void filter_withMoreThanOneFilter() {

        entityRequest.asUser(DEFAULT_USER).makeTask("title", "Take dog for a walk", "complete", "true");
        entityRequest.asUser(DEFAULT_USER).makeTask("title", "Take dog to vet", "complete", "false");
        entityRequest.asUser(DEFAULT_USER).makeTask("title", "Feed dog", "complete", "true");

        request.asUser(DEFAULT_USER).to("tasks")
                .withParam("title", "dog")
                .withParam("complete", "true")
                .get().isOk()
                .hasField("$._embedded.tasks.length()", v -> v.isEqualTo(2))
                .hasField("$._embedded.tasks[?(@.title == 'Take dog for a walk')]", JsonPathAssertions::exists)
                .hasField("$._embedded.tasks[?(@.title == 'Feed dog')]", JsonPathAssertions::exists);

    }

}
