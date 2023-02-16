package degallant.github.io.todoapp.domain;

import degallant.github.io.todoapp.test.IntegrationTest;
import org.junit.jupiter.api.Test;

import java.util.UUID;

public class CommentsTests extends IntegrationTest {

    @Test
    public void post_failsWhenTaskIsInvalid() {

        var taskId = entityRequest.asUser(DEFAULT_USER).makeTask("Task A");
        var anotherId = entityRequest.asUser(ANOTHER_USER).makeTask("Task X");

        request.asUser(DEFAULT_USER).to("tasks/invalid/comments").withField("text", "comment").post().isNotFound();
        request.asUser(DEFAULT_USER).to("tasks/" + UUID.randomUUID() + "/comments").withField("text", "comment").post().isNotFound();
        request.asUser(DEFAULT_USER).to("tasks/" + anotherId.uuid() + "/comments").withField("text", "comment").post().isNotFound();

        request.asUser(DEFAULT_USER).to(taskId.uri()).delete().isNoContent();
        request.asUser(DEFAULT_USER).to("tasks/" + taskId.uuid() + "/comments").withField("text", "comment").post().isNotFound();

    }

    @Test
    public void get_failsWhenTaskIsInvalid() {

        var taskId = entityRequest.asUser(DEFAULT_USER).makeTask("Task A");
        var anotherId = entityRequest.asUser(ANOTHER_USER).makeTask("Task X");

        request.asUser(DEFAULT_USER).to("tasks/invalid/comments").get().isNotFound();
        request.asUser(DEFAULT_USER).to("tasks/" + UUID.randomUUID() + "/comments").get().isNotFound();
        request.asUser(DEFAULT_USER).to("tasks/" + anotherId.uuid() + "/comments").get().isNotFound();

        request.asUser(DEFAULT_USER).to(taskId.uri()).delete().isNoContent();
        request.asUser(DEFAULT_USER).to("tasks/" + taskId.uuid() + "/comments").get().isNotFound();

    }

    @Test
    public void patch_failsWhenTaskOrCommentIsInvalid() {

        var taskId = entityRequest.asUser(DEFAULT_USER).makeTask("Task A");
        var commentId = entityRequest.asUser(DEFAULT_USER).commentOnTask(taskId.uuid(), "Comment A");
        var anotherId = entityRequest.asUser(ANOTHER_USER).makeTask("Task X");

        request.asUser(DEFAULT_USER).to("tasks/invalid/comments/" + commentId.uuid()).withField("text", "new").patch().isNotFound();
        request.asUser(DEFAULT_USER).to("tasks/" + UUID.randomUUID() + "/comments/" + commentId.uuid()).withField("text", "new").patch().isNotFound();
        request.asUser(DEFAULT_USER).to("tasks/" + anotherId.uuid() + "/comments/" + commentId.uuid()).withField("text", "new").patch().isNotFound();

        request.asUser(DEFAULT_USER).to(taskId.uri()).delete().isNoContent();
        request.asUser(DEFAULT_USER).to("tasks/" + taskId.uuid() + "/comments/" + commentId.uuid()).withField("text", "new").patch().isNotFound();

        var newTaskId = entityRequest.asUser(DEFAULT_USER).makeTask("Task A");
        var newCommentId = entityRequest.asUser(DEFAULT_USER).commentOnTask(newTaskId.uuid(), "Comment A");
        request.asUser(DEFAULT_USER).to(newCommentId.uri()).delete().isNoContent();
        request.asUser(DEFAULT_USER).to("tasks/" + newTaskId.uuid() + "/comments/" + newCommentId.uuid()).withField("text", "new").patch().isNotFound();

    }

    @Test
    public void delete_failsWhenTaskOrCommentIsInvalid() {

        var taskId = entityRequest.asUser(DEFAULT_USER).makeTask("Task A");
        var commentId = entityRequest.asUser(DEFAULT_USER).commentOnTask(taskId.uuid(), "Comment A");
        var anotherId = entityRequest.asUser(ANOTHER_USER).makeTask("Task X");

        request.asUser(DEFAULT_USER).to("tasks/invalid/comments/" + commentId.uuid()).delete().isNotFound();
        request.asUser(DEFAULT_USER).to("tasks/" + UUID.randomUUID() + "/comments/" + commentId.uuid()).delete().isNotFound();
        request.asUser(DEFAULT_USER).to("tasks/" + anotherId.uuid() + "/comments/" + commentId.uuid()).delete().isNotFound();

        request.asUser(DEFAULT_USER).to(taskId.uri()).delete().isNoContent();
        request.asUser(DEFAULT_USER).to("tasks/" + taskId.uuid() + "/comments/" + commentId.uuid()).delete().isNotFound();

        var newTaskId = entityRequest.asUser(DEFAULT_USER).makeTask("Task A");
        var newCommentId = entityRequest.asUser(DEFAULT_USER).commentOnTask(newTaskId.uuid(), "Comment A");
        request.asUser(DEFAULT_USER).to(newCommentId.uri()).delete().isNoContent();
        request.asUser(DEFAULT_USER).to("tasks/" + newTaskId.uuid() + "/comments/" + newCommentId.uuid()).delete().isNotFound();

    }

    @Test
    public void get_failsWhenTaskOrCommentIsInvalid() {

        var taskId = entityRequest.asUser(DEFAULT_USER).makeTask("Task A");
        var commentId = entityRequest.asUser(DEFAULT_USER).commentOnTask(taskId.uuid(), "Comment A");
        var anotherId = entityRequest.asUser(ANOTHER_USER).makeTask("Task X");

        request.asUser(DEFAULT_USER).to("tasks/invalid/comments/" + commentId.uuid()).get().isNotFound();
        request.asUser(DEFAULT_USER).to("tasks/" + UUID.randomUUID() + "/comments/" + commentId.uuid()).get().isNotFound();
        request.asUser(DEFAULT_USER).to("tasks/" + anotherId.uuid() + "/comments/" + commentId.uuid()).get().isNotFound();

        request.asUser(DEFAULT_USER).to(taskId.uri()).delete().isNoContent();
        request.asUser(DEFAULT_USER).to("tasks/" + taskId.uuid() + "/comments/" + commentId.uuid()).get().isNotFound();

        var newTaskId = entityRequest.asUser(DEFAULT_USER).makeTask("Task A");
        var newCommentId = entityRequest.asUser(DEFAULT_USER).commentOnTask(newTaskId.uuid(), "Comment A");
        request.asUser(DEFAULT_USER).to(newCommentId.uri()).delete().isNoContent();
        request.asUser(DEFAULT_USER).to("tasks/" + newTaskId.uuid() + "/comments/" + newCommentId.uuid()).get().isNotFound();

    }

    @Test
    public void post_failsWhenDataIsInvalid() {

        var taskId = entityRequest.asUser(DEFAULT_USER).makeTask("Task A");

        request.asUser(DEFAULT_USER).to("tasks/" + taskId.uuid() + "/comments").withField("text", "")
                .post().isBadRequest().hasField("$.errors[0].type", contains("validation.is_empty"));

        request.asUser(DEFAULT_USER).to("tasks/" + taskId.uuid() + "/comments").withField("random", "")
                .post().isBadRequest().hasField("$.errors[0].type", contains("validation.is_required"));

    }

    @Test
    public void patch_failsWhenDataIsInvalid() {

        var taskId = entityRequest.asUser(DEFAULT_USER).makeTask("Task A");
        var commentId = entityRequest.asUser(DEFAULT_USER).commentOnTask(taskId.uuid(), "Comment A");

        request.asUser(DEFAULT_USER).to(commentId.uri()).withField("text", "")
                .patch().isBadRequest().hasField("$.errors[0].type", contains("validation.is_empty"));

    }

    @Test
    public void post_createComment() {

        var id = entityRequest.asUser(DEFAULT_USER).makeTask("Take dog to vet").uuid();
        var uri = request.asUser(DEFAULT_USER).to("tasks/" + id + "/comments")
                .withField("text", "a comment")
                .post().isCreated()
                .getLocation();

        request.asUser(DEFAULT_USER).to(uri)
                .get().isOk()
                .hasField("$.text", isEqualTo("a comment"));

    }

    @Test
    public void details_showCommentDetails() {

        var id = entityRequest.asUser(DEFAULT_USER).makeTask("Take dog to vet").uuid();
        var commentId = entityRequest.asUser(DEFAULT_USER).commentOnTask(id, "gotta rush");

        request.asUser(DEFAULT_USER).to(commentId.uri())
                .get().isOk()
                .hasField("$.text", isEqualTo("gotta rush"))
                .hasField("$.commented_at", exists())
                .hasField("$.edited_at", exists());

    }

    @Test
    public void list_noItems() {

        var id = entityRequest.asUser(DEFAULT_USER).makeTask("Task A").uuid();

        request.asUser(DEFAULT_USER).to("tasks/" + id + "/comments")
                .get().isOk().hasField("$._embedded.comments.length()", isEqualTo(0));

    }

    @Test
    public void list_allComments() {

        var id = entityRequest.asUser(DEFAULT_USER).makeTaskWithDetails("title", "Task A").uuid();
        entityRequest.asUser(DEFAULT_USER).commentOnTask(id, "Comment A", "Comment B");

        request.asUser(DEFAULT_USER).to("tasks/" + id + "/comments")
                .get().isOk()
                .hasField("$._embedded.comments.length()", isEqualTo(2))
                .hasField("$._embedded.comments[?(@.text == 'Comment A')]", exists())
                .hasField("$._embedded.comments[?(@.text == 'Comment B')]", exists());

    }

    @Test
    public void list_ignoreDeletedComments() {

        var id = entityRequest.asUser(DEFAULT_USER).makeTaskWithDetails("title", "Task A").uuid();
        var commentIds = entityRequest.asUser(DEFAULT_USER).commentOnTask(id, "Comment A", "Comment B", "Comment C");

        request.asUser(DEFAULT_USER).to(commentIds.get(1).uri()).delete().isNoContent();

        request.asUser(DEFAULT_USER).to("tasks/" + id + "/comments")
                .get().isOk()
                .hasField("$._embedded.comments.length()", isEqualTo(2))
                .hasField("$._embedded.comments[?(@.text == 'Comment A')]", exists())
                .hasField("$._embedded.comments[?(@.text == 'Comment C')]", exists());

    }

    @Test
    public void patch_updatesNothing() {

        var taskId = entityRequest.asUser(DEFAULT_USER).makeTask("Task A");
        var commentId = entityRequest.asUser(DEFAULT_USER).commentOnTask(taskId.uuid(), "Comment A");
        var path = "tasks/" + taskId.uuid() + "/comments/" + commentId.uuid();

        request.asUser(DEFAULT_USER).to(path).withField("invalid", "random").patch().isNoContent();

    }

    @Test
    public void patch_updatesText() {

        var taskId = entityRequest.asUser(DEFAULT_USER).makeTask("Task A");
        var commentId = entityRequest.asUser(DEFAULT_USER).commentOnTask(taskId.uuid(), "Comment A");
        var path = "tasks/" + taskId.uuid() + "/comments/" + commentId.uuid();

        request.asUser(DEFAULT_USER).to(path).withField("text", "New text").patch().isOk();
        request.asUser(DEFAULT_USER).to(path).get().isOk().hasField("$.text", isEqualTo("New text"));

    }

}
