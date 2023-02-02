package degallant.github.io.todoapp;

import degallant.github.io.todoapp.common.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.reactive.server.JsonPathAssertions;

import java.util.UUID;

import static org.hamcrest.Matchers.containsString;

public class CommentsTests extends IntegrationTest {

    @Test
    public void create_failsWithInvalidTaskId() {

        request.asUser(DEFAULT_USER).to("tasks/invalid/comments")
                .withField("text", "a comment")
                .post().isNotFound();

    }

    @Test
    public void create_failsWithIdThatDoesNotExists() {

        var id = UUID.randomUUID();
        request.asUser(DEFAULT_USER).to("tasks/" + id + "/comments")
                .withField("text", "a comment")
                .post().isNotFound();

    }

    @Test
    public void create_failsWithEmptyText() {

        var id = entityRequest.asUser(DEFAULT_USER).makeTaskWithDetails("title", "Take dog to vet").uuid();
        request.asUser(DEFAULT_USER).to("tasks/" + id + "/comments")
                .withField("text", "")
                .post().isBadRequest()
                .hasField("$.errors[0].type", v -> v.value(containsString("validation.is_empty")));

    }

    @Test
    public void create_commentInATask() {

        var id = entityRequest.asUser(DEFAULT_USER).makeTaskWithDetails("title", "Take dog to vet").uuid();
        var uri = request.asUser(DEFAULT_USER).to("tasks/" + id + "/comments")
                .withField("text", "a comment")
                .post().isCreated()
                .getLocation();

        request.asUser(DEFAULT_USER).to(uri)
                .get().isOk()
                .hasField("$.text", v -> v.isEqualTo("a comment"));

    }

    @Test
    public void user_canCommentOnlyOnItsTasks() {

        var id = entityRequest.asUser("another@gmail.com").makeTaskWithDetails("title", "Take dog to vet").uuid();
        request.asUser(DEFAULT_USER).to("tasks/" + id + "/comments")
                .withField("text", "a comment")
                .post().isNotFound();

    }

    @Test
    public void details_failsWithInvalidTask() {

        request.asUser(DEFAULT_USER).to("tasks/invalid/comments/invalid")
                .get().isNotFound();

    }

    @Test
    public void details_failsWithUnknownTask() {

        var id = UUID.randomUUID();
        request.asUser(DEFAULT_USER).to("tasks/" + id + "/comments/invalid")
                .get().isNotFound();

    }

    @Test
    public void details_failsWithInvalidComment() {

        var id = entityRequest.asUser(DEFAULT_USER).makeTaskWithDetails("title", "Take dog to vet").uuid();
        request.asUser(DEFAULT_USER).to("tasks/" + id + "/comments/invalid")
                .get().isNotFound();

    }

    @Test
    public void details_failsWithUnknownComment() {

        var id = entityRequest.asUser(DEFAULT_USER).makeTaskWithDetails("title", "Take dog to vet").uuid();
        var commentId = UUID.randomUUID();
        request.asUser(DEFAULT_USER).to("tasks/" + id + "/comments/" + commentId)
                .get().isNotFound();

    }

    @Test
    public void details_commentText() {

        var id = entityRequest.asUser(DEFAULT_USER).makeTaskWithDetails("title", "Take dog to vet").uuid();
        var commentId = entityRequest.asUser(DEFAULT_USER).commentOnTask(id, "gotta rush").uuid();
        request.asUser(DEFAULT_USER).to("tasks/" + id + "/comments/" + commentId)
                .get().isOk()
                .hasField("$.text", v -> v.isEqualTo("gotta rush"));

    }

    @Test
    public void user_canOnlySeeItsComments() {

        var id = entityRequest.asUser("another@gmail.com").makeTaskWithDetails("title", "Take dog to vet").uuid();
        var commentId = entityRequest.asUser("another@gmail.com").commentOnTask(id, "gotta rush").uuid();
        request.asUser(DEFAULT_USER).to("tasks/" + id + "/comments/" + commentId)
                .get().isNotFound();

    }

    @Test
    public void list_failsWithInvalidTaskId() {

        request.asUser(DEFAULT_USER).to("tasks/invalid/comments")
                .withField("text", "a comment")
                .get().isNotFound();

    }

    @Test
    public void list_failsWithIdThatDoesNotExists() {

        var id = UUID.randomUUID();
        request.asUser(DEFAULT_USER).to("tasks/" + id + "/comments")
                .get().isNotFound();

    }

    @Test
    public void list_noItems() {

        var id = entityRequest.asUser(DEFAULT_USER).makeTaskWithDetails("title", "Task A").uuid();
        request.asUser(DEFAULT_USER).to("tasks/" + id + "/comments")
                .get()
                .hasField("$._embedded.comments.length()", v -> v.isEqualTo(0));

    }

    @Test
    public void list_allComments() {

        var id = entityRequest.asUser(DEFAULT_USER).makeTaskWithDetails("title", "Task A").uuid();
        entityRequest.asUser(DEFAULT_USER).commentOnTask(id, "Comment A", "Comment B");

        request.asUser(DEFAULT_USER).to("tasks/" + id + "/comments")
                .get()
                .hasField("$._embedded.comments.length()", v -> v.isEqualTo(2))
                .hasField("$._embedded.comments[?(@.text == 'Comment A')]", JsonPathAssertions::exists)
                .hasField("$._embedded.comments[?(@.text == 'Comment B')]", JsonPathAssertions::exists);

    }

}
