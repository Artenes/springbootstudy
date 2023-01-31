package degallant.github.io.todoapp.tasks;

import degallant.github.io.todoapp.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.reactive.server.JsonPathAssertions;

import java.net.URI;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;

public class TasksPatchTests extends IntegrationTest {

    @Test
    public void taskPatched_patchAllFields() {

        var title = "Take the dog for a walk";
        var description = "This is very important, dog needs to walk or it will not behave";
        var dueDate = "2030-01-01T12:50:29.790511-04:00";
        var priority = "P3";
        var complete = "true";
        var parentId = makeTaskAsUser(DEFAULT_USER, "Parent task").uuid().toString();
        var tags = makeTagsAsUser(DEFAULT_USER, "daily", "home", "pet");
        var projectId = makeProjectAsUser(DEFAULT_USER, "daily tasks");

        URI taskUri = request.asUser(DEFAULT_USER).to("tasks")
                .withField("title", "Title")
                .withField("tags_ids", makeTagsAsUser(DEFAULT_USER, "Tag A"))
                .post().isCreated().getLocation();

        request.asUser(DEFAULT_USER).to(taskUri)
                .withField("title", title)
                .withField("description", description)
                .withField("due_date", dueDate)
                .withField("priority", priority)
                .withField("complete", complete)
                .withField("parent_id", parentId)
                .withField("project_id", projectId)
                .withField("tags_ids", tags)
                .patch().isOk();

        request.asUser(DEFAULT_USER).to(taskUri)
                .get().isOk()
                .show()
                .hasField("$.title", value -> value.isEqualTo(title))
                .hasField("$.description", value -> value.isEqualTo(description))
                .hasField("$.due_date", value -> value.isEqualTo(dueDate))
                .hasField("$.priority", value -> value.isEqualTo(priority))
                .hasField("$.complete", value -> value.isEqualTo(complete))
                .hasField("$._embedded.parent.id", value -> value.isEqualTo(parentId))
                .hasField("$._embedded.project.id", value -> value.isEqualTo(projectId))
                .hasField("$._embedded.tags.length()", value -> value.isEqualTo(3))
                .hasField("$._embedded.tags[?(@.name == 'daily')]", JsonPathAssertions::exists)
                .hasField("$._embedded.tags[?(@.name == 'home')]", JsonPathAssertions::exists)
                .hasField("$._embedded.tags[?(@.name == 'pet')]", JsonPathAssertions::exists);

    }

    @Test
    public void taskPatched_doNotUpdateFields_whenAllFieldsAreNull() {

        var title = "Take the dog for a walk";
        var description = "This is very important, dog needs to walk or it will not behave";
        var dueDate = "2030-01-01T12:50:29.790511-04:00";
        var priority = "P3";
        var complete = "true";
        var parentId = makeTaskAsUser(DEFAULT_USER, "Parent task").uuid().toString();
        var tags = makeTagsAsUser(DEFAULT_USER, "daily", "home", "pet");
        var projectId = makeProjectAsUser(DEFAULT_USER, "daily tasks");

        URI taskUri = request.asUser(DEFAULT_USER).to("tasks")
                .withField("title", title)
                .withField("description", description)
                .withField("due_date", dueDate)
                .withField("priority", priority)
                .withField("complete", complete)
                .withField("parent_id", parentId)
                .withField("project_id", projectId)
                .withField("tags_ids", tags)
                .post().isCreated().getLocation();

        request.asUser(DEFAULT_USER).to(taskUri).patch().isOk();

        request.asUser(DEFAULT_USER).to(taskUri)
                .get().isOk()
                .hasField("$.title", value -> value.isEqualTo(title))
                .hasField("$.description", value -> value.isEqualTo(description))
                .hasField("$.due_date", value -> value.isEqualTo(dueDate))
                .hasField("$.priority", value -> value.isEqualTo(priority))
                .hasField("$.complete", value -> value.isEqualTo(complete))
                .hasField("$._embedded.parent.id", value -> value.isEqualTo(parentId))
                .hasField("$._embedded.project.id", value -> value.isEqualTo(projectId))
                .hasField("$._embedded.tags.length()", value -> value.isEqualTo(3))
                .hasField("$._embedded.tags[?(@.name == 'daily')]", JsonPathAssertions::exists)
                .hasField("$._embedded.tags[?(@.name == 'home')]", JsonPathAssertions::exists)
                .hasField("$._embedded.tags[?(@.name == 'pet')]", JsonPathAssertions::exists);

    }

    @Test
    public void taskNotPatched_invalidTitle_isEmpty() {
        var uri = makeTaskAsUser(DEFAULT_USER, "Take dog for a walk").uri();
        request.asUser(DEFAULT_USER).to(uri)
                .withField("title", "").patch()
                .isBadRequest()
                .hasField("$.errors.length()", v -> v.isEqualTo(1))
                .hasField("$.errors[0].field", v -> v.isEqualTo("title"))
                .hasField("$.errors[0].type", v -> v.value(containsString("validation.is_empty")));

    }

    @Test
    public void taskNotPatched_invalidDescription_isEmpty() {

        var uri = makeTaskAsUser(DEFAULT_USER, "Take dog for a walk").uri();
        request.asUser(DEFAULT_USER).to(uri)
                .withField("title", "Go for a walk")
                .withField("description", "").patch()
                .isBadRequest()
                .hasField("$.errors.length()", v -> v.isEqualTo(1))
                .hasField("$.errors[0].field", v -> v.isEqualTo("description"))
                .hasField("$.errors[0].type", v -> v.value(containsString("validation.is_empty")));
    }

    @Test
    public void taskNotPatched_invalidDueDate_isEmpty() {

        var uri = makeTaskAsUser(DEFAULT_USER, "Take dog for a walk").uri();
        request.asUser(DEFAULT_USER).to(uri)
                .withField("title", "Go for a walk")
                .withField("due_date", "").patch()
                .isBadRequest()
                .hasField("$.errors.length()", v -> v.isEqualTo(1))
                .hasField("$.errors[0].field", v -> v.isEqualTo("due_date"))
                .hasField("$.errors[0].type", v -> v.value(containsString("validation.not_a_valid_date")));
    }

    @Test
    public void taskNotPatched_invalidDueDate_isInPast() {

        var uri = makeTaskAsUser(DEFAULT_USER, "Take dog for a walk").uri();
        request.asUser(DEFAULT_USER).to(uri)
                .withField("title", "Go for a walk")
                .withField("due_date", "2001-01-01T12:50:29.790511-04:00").patch()
                .isBadRequest()
                .hasField("$.errors.length()", v -> v.isEqualTo(1))
                .hasField("$.errors[0].field", v -> v.isEqualTo("due_date"))
                .hasField("$.errors[0].type", v -> v.value(containsString("validation.is_present_or_future")));
    }

    @Test
    public void taskNotPatched_invalidPriority() {

        var uri = makeTaskAsUser(DEFAULT_USER, "Take dog for a walk").uri();
        request.asUser(DEFAULT_USER).to(uri)
                .withField("title", "Go for a walk")
                .withField("priority", "invalid").patch()
                .isBadRequest()
                .hasField("$.errors.length()", v -> v.isEqualTo(1))
                .hasField("$.errors[0].field", v -> v.isEqualTo("priority"))
                .hasField("$.errors[0].type", v -> v.value(containsString("validation.is_priority")));
    }

    @Test
    public void taskNotPatched_invalidTagsIds() {

        var uri = makeTaskAsUser(DEFAULT_USER, "Take dog for a walk").uri();
        request.asUser(DEFAULT_USER).to(uri)
                .withField("title", "Go for a walk")
                .withField("tags_ids", "invalid").patch()
                .isBadRequest()
                .hasField("$.errors.length()", v -> v.isEqualTo(1))
                .hasField("$.errors[0].field", v -> v.isEqualTo("tags_ids"))
                .hasField("$.errors[0].type", v -> v.value(containsString("validation.invalid_id_list")));
    }

    @Test
    public void taskNotPatched_invalidTagsIds_isWithNonexistentIds() {

        var uri = makeTaskAsUser(DEFAULT_USER, "Take dog for a walk").uri();
        request.asUser(DEFAULT_USER).to(uri)
                .withField("title", "Go for a walk")
                .withArray("tags_ids", UUID.randomUUID(), UUID.randomUUID()).patch()
                .isBadRequest()
                .hasField("$.errors.length()", v -> v.isEqualTo(1))
                .hasField("$.errors[0].field", v -> v.isEqualTo("tags_ids"))
                .hasField("$.errors[0].type", v -> v.value(containsString("validation.do_not_exist_list")));
    }

    @Test
    public void taskNotPatched_invalidTagsIds_isWithIdsFromOtherUser() {

        UUID tagAId = request.asUser("another@gmail.com").to("tags")
                .withField("name", "Tag A").post()
                .isCreated()
                .getLocationUUID();

        var uri = makeTaskAsUser(DEFAULT_USER, "Take dog for a walk").uri();
        request.asUser(DEFAULT_USER).to(uri)
                .withField("title", "Go for a walk")
                .withArray("tags_ids", tagAId).patch()
                .isBadRequest()
                .hasField("$.errors.length()", v -> v.isEqualTo(1))
                .hasField("$.errors[0].field", v -> v.isEqualTo("tags_ids"))
                .hasField("$.errors[0].type", v -> v.value(containsString("validation.do_not_exist_list")));
    }

    @Test
    public void taskNotPatched_invalidParentIds() {

        var uri = makeTaskAsUser(DEFAULT_USER, "Take dog for a walk").uri();
        request.asUser(DEFAULT_USER).to(uri)
                .withField("title", "Go for a walk")
                .withField("parent_id", "invalid").patch()
                .isBadRequest()
                .hasField("$.errors.length()", v -> v.isEqualTo(1))
                .hasField("$.errors[0].field", v -> v.isEqualTo("parent_id"))
                .hasField("$.errors[0].type", v -> v.value(containsString("validation.is_uuid")));
    }

    @Test
    public void taskNotPatched_invalidParentId_withNonexistentId() {

        var uri = makeTaskAsUser(DEFAULT_USER, "Take dog for a walk").uri();
        request.asUser(DEFAULT_USER).to(uri)
                .withField("title", "Go for a walk")
                .withField("parent_id", UUID.randomUUID()).patch()
                .isBadRequest()
                .hasField("$.errors.length()", v -> v.isEqualTo(1))
                .hasField("$.errors[0].field", v -> v.isEqualTo("parent_id"))
                .hasField("$.errors[0].type", v -> v.value(containsString("validation.do_not_exist")));
    }

    @Test
    public void taskNotPatched_invalidParentId_isWithIdFromOtherUser() {
        UUID taskId = request.asUser("another@gmail.com").to("tasks")
                .withField("title", "Task A").post()
                .isCreated()
                .getLocationUUID();

        var uri = makeTaskAsUser(DEFAULT_USER, "Take dog for a walk").uri();
        request.asUser(DEFAULT_USER).to(uri)
                .withField("title", "Go for a walk")
                .withField("parent_id", taskId).patch()
                .isBadRequest()
                .hasField("$.errors.length()", v -> v.isEqualTo(1))
                .hasField("$.errors[0].field", v -> v.isEqualTo("parent_id"))
                .hasField("$.errors[0].type", v -> v.value(containsString("validation.do_not_exist")));
    }

    @Test
    public void taskNotPatched_invalidProjectId() {
        var uri = makeTaskAsUser(DEFAULT_USER, "Take dog for a walk").uri();
        request.asUser(DEFAULT_USER).to(uri)
                .withField("title", "Go for a walk")
                .withField("project_id", "invalid").patch()
                .isBadRequest()
                .hasField("$.errors.length()", v -> v.isEqualTo(1))
                .hasField("$.errors[0].field", v -> v.isEqualTo("project_id"))
                .hasField("$.errors[0].type", v -> v.value(containsString("validation.is_uuid")));
    }

    @Test
    public void taskNotPatched_invalidProjectId_withNonexistentId() {
        var uri = makeTaskAsUser(DEFAULT_USER, "Take dog for a walk").uri();
        request.asUser(DEFAULT_USER).to(uri)
                .withField("title", "Go for a walk")
                .withField("project_id", UUID.randomUUID()).patch()
                .isBadRequest()
                .hasField("$.errors.length()", v -> v.isEqualTo(1))
                .hasField("$.errors[0].field", v -> v.isEqualTo("project_id"))
                .hasField("$.errors[0].type", v -> v.value(containsString("validation.do_not_exist")));
    }

    @Test
    public void taskNotPatched_invalidProjectId_isWithIdFromOtherUser() {
        UUID projectId = request.asUser("another@gmail.com").to("projects")
                .withField("title", "Project A").post()
                .isCreated()
                .getLocationUUID();

        var uri = makeTaskAsUser(DEFAULT_USER, "Take dog for a walk").uri();
        request.asUser(DEFAULT_USER).to(uri)
                .withField("title", "Go for a walk")
                .withField("project_id", projectId).patch()
                .isBadRequest()
                .hasField("$.errors.length()", v -> v.isEqualTo(1))
                .hasField("$.errors[0].field", v -> v.isEqualTo("project_id"))
                .hasField("$.errors[0].type", v -> v.value(containsString("validation.do_not_exist")));
    }

}
