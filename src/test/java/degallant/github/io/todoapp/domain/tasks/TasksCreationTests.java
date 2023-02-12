package degallant.github.io.todoapp.domain.tasks;

import degallant.github.io.todoapp.test.IntegrationTest;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.UUID;

public class TasksCreationTests extends IntegrationTest {

    @Test
    public void taskCreated_withAllFields() {

        var title = "Take the dog for a walk";
        var description = "This is very important, dog needs to walk or it will not behave";
        var dueDate = "2030-01-01T12:50:29.790511-04:00";
        var priority = "P3";
        var complete = "true";
        var parentId = entityRequest.asUser(DEFAULT_USER).makeTask("Parent task").uuid();
        var tags = entityRequest.asUser(DEFAULT_USER).makeTags("daily", "home", "pet").asString();
        var projectId = entityRequest.asUser(DEFAULT_USER).makeProject("daily tasks").uuid();

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

        request.asUser(DEFAULT_USER).to(taskUri)
                .get().isOk()
                .show()
                .hasField("$.title", isEqualTo(title))
                .hasField("$.description", isEqualTo(description))
                .hasField("$.due_date", isEqualTo(dueDate))
                .hasField("$.priority", isEqualTo(priority))
                .hasField("$.complete", isEqualTo(complete))
                .hasField("$._embedded.parent.id", isEqualTo(parentId))
                .hasField("$._embedded.project.id", isEqualTo(projectId))
                .hasField("$._embedded.tags.length()", isEqualTo(3))
                .hasField("$._embedded.tags[?(@.name == 'daily')]", exists())
                .hasField("$._embedded.tags[?(@.name == 'home')]", exists())
                .hasField("$._embedded.tags[?(@.name == 'pet')]", exists());

    }

    @Test
    public void taskNotCreated_invalidTitle_isEmpty() {
        request.asUser(DEFAULT_USER).to("tasks")
                .withField("title", "").post()
                .isBadRequest()
                .hasField("$.errors.length()", isEqualTo(1))
                .hasField("$.errors[0].field", isEqualTo("title"))
                .hasField("$.errors[0].type", contains("validation.is_empty"));
    }

    @Test
    public void taskNotCreated_invalidDescription_isEmpty() {
        request.asUser(DEFAULT_USER).to("tasks")
                .withField("title", "Go for a walk")
                .withField("description", "").post()
                .isBadRequest()
                .hasField("$.errors.length()", isEqualTo(1))
                .hasField("$.errors[0].field", isEqualTo("description"))
                .hasField("$.errors[0].type", contains("validation.is_empty"));
    }

    @Test
    public void taskNotCreated_invalidDueDate_isEmpty() {
        request.asUser(DEFAULT_USER).to("tasks")
                .withField("title", "Go for a walk")
                .withField("due_date", "").post()
                .isBadRequest()
                .hasField("$.errors.length()", isEqualTo(1))
                .hasField("$.errors[0].field", isEqualTo("due_date"))
                .hasField("$.errors[0].type", contains("validation.not_a_valid_date"));
    }

    @Test
    public void taskNotCreated_invalidDueDate_isInPast() {
        request.asUser(DEFAULT_USER).to("tasks")
                .withField("title", "Go for a walk")
                .withField("due_date", "2001-01-01T12:50:29.790511-04:00").post()
                .isBadRequest()
                .hasField("$.errors.length()", isEqualTo(1))
                .hasField("$.errors[0].field", isEqualTo("due_date"))
                .hasField("$.errors[0].type", contains("validation.is_present_or_future"));
    }

    @Test
    public void taskNotCreated_invalidPriority() {
        request.asUser(DEFAULT_USER).to("tasks")
                .withField("title", "Go for a walk")
                .withField("priority", "invalid").post()
                .isBadRequest()
                .hasField("$.errors.length()", isEqualTo(1))
                .hasField("$.errors[0].field", isEqualTo("priority"))
                .hasField("$.errors[0].type", contains("validation.is_priority"));
    }

    @Test
    public void taskNotCreated_invalidTagsIds() {
        request.asUser(DEFAULT_USER).to("tasks")
                .withField("title", "Go for a walk")
                .withField("tags_ids", "invalid").post()
                .isBadRequest()
                .hasField("$.errors.length()", isEqualTo(1))
                .hasField("$.errors[0].field", isEqualTo("tags_ids"))
                .hasField("$.errors[0].type", contains("validation.invalid_id_list"));
    }

    @Test
    public void taskNotCreated_invalidTagsIds_isWithNonexistentIds() {
        request.asUser(DEFAULT_USER).to("tasks")
                .withField("title", "Go for a walk")
                .withArray("tags_ids", UUID.randomUUID(), UUID.randomUUID()).post()
                .isBadRequest()
                .hasField("$.errors.length()", isEqualTo(1))
                .hasField("$.errors[0].field", isEqualTo("tags_ids"))
                .hasField("$.errors[0].type", contains("validation.do_not_exist_list"));
    }

    @Test
    public void taskNotCreated_invalidTagsIds_isWithIdsFromOtherUser() {
        UUID tagAId = request.asUser("another@gmail.com").to("tags")
                .withField("name", "Tag A").post()
                .isCreated()
                .getLocationUUID();

        request.asUser(DEFAULT_USER).to("tasks")
                .withField("title", "Go for a walk")
                .withArray("tags_ids", tagAId).post()
                .isBadRequest()
                .hasField("$.errors.length()", isEqualTo(1))
                .hasField("$.errors[0].field", isEqualTo("tags_ids"))
                .hasField("$.errors[0].type", contains("validation.do_not_exist_list"));
    }

    @Test
    public void taskNotCreated_invalidParentIds() {
        request.asUser(DEFAULT_USER).to("tasks")
                .withField("title", "Go for a walk")
                .withField("parent_id", "invalid").post()
                .isBadRequest()
                .hasField("$.errors.length()", isEqualTo(1))
                .hasField("$.errors[0].field", isEqualTo("parent_id"))
                .hasField("$.errors[0].type", contains("validation.do_not_exist"));
    }

    @Test
    public void taskNotCreated_invalidParentId_withNonexistentId() {
        request.asUser(DEFAULT_USER).to("tasks")
                .withField("title", "Go for a walk")
                .withField("parent_id", UUID.randomUUID()).post()
                .isBadRequest()
                .hasField("$.errors.length()", isEqualTo(1))
                .hasField("$.errors[0].field", isEqualTo("parent_id"))
                .hasField("$.errors[0].type", contains("validation.do_not_exist"));
    }

    @Test
    public void taskNotCreated_invalidParentId_isWithIdFromOtherUser() {
        UUID taskId = request.asUser("another@gmail.com").to("tasks")
                .withField("title", "Task A").post()
                .isCreated()
                .getLocationUUID();

        request.asUser(DEFAULT_USER).to("tasks")
                .withField("title", "Go for a walk")
                .withField("parent_id", taskId).post()
                .isBadRequest()
                .hasField("$.errors.length()", isEqualTo(1))
                .hasField("$.errors[0].field", isEqualTo("parent_id"))
                .hasField("$.errors[0].type", contains("validation.do_not_exist"));
    }

    @Test
    public void taskNotCreated_invalidProjectId() {
        request.asUser(DEFAULT_USER).to("tasks")
                .withField("title", "Go for a walk")
                .withField("project_id", "invalid").post()
                .isBadRequest()
                .hasField("$.errors.length()", isEqualTo(1))
                .hasField("$.errors[0].field", isEqualTo("project_id"))
                .hasField("$.errors[0].type", contains("validation.do_not_exist"));
    }

    @Test
    public void taskNotCreated_invalidProjectId_withNonexistentId() {
        request.asUser(DEFAULT_USER).to("tasks")
                .withField("title", "Go for a walk")
                .withField("project_id", UUID.randomUUID()).post()
                .isBadRequest()
                .hasField("$.errors.length()", isEqualTo(1))
                .hasField("$.errors[0].field", isEqualTo("project_id"))
                .hasField("$.errors[0].type", contains("validation.do_not_exist"));
    }

    @Test
    public void taskNotCreated_invalidProjectId_isWithIdFromOtherUser() {
        UUID projectId = request.asUser("another@gmail.com").to("projects")
                .withField("title", "Project A").post()
                .isCreated()
                .getLocationUUID();

        request.asUser(DEFAULT_USER).to("tasks")
                .withField("title", "Go for a walk")
                .withField("project_id", projectId).post()
                .isBadRequest()
                .hasField("$.errors.length()", isEqualTo(1))
                .hasField("$.errors[0].field", isEqualTo("project_id"))
                .hasField("$.errors[0].type", contains("validation.do_not_exist"));
    }

}
