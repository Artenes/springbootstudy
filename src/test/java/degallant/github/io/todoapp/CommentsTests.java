package degallant.github.io.todoapp;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.Map;

/**
 * @noinspection ConstantConditions
 */
public class CommentsTests extends IntegrationTest {

    @Test
    public void userCanAddCommentToATask() {

        var comment = "We have to finish this soon";
        authenticate();

        URI taskUri = client.post().uri("/v1/tasks")
                .bodyValue(Map.of("title", "Walk with dog"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody().returnResult().getResponseHeaders().getLocation();

        URI commentsUri = URI.create(taskUri.toString() + "/comments");

        URI commentUri = client.post().uri(commentsUri)
                .bodyValue(Map.of("text", comment))
                .exchange()
                .expectStatus().isCreated()
                .expectBody().returnResult()
                .getResponseHeaders().getLocation();

        show();

        client.get().uri(commentUri)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.text").isEqualTo(comment);

    }

    @Test
    public void userCanOnlyCommentOnItsTasks() {

        authenticate("usera@gmail.com");
        URI taskUri = client.post().uri("/v1/tasks")
                .bodyValue(Map.of("title", "Walk with dog"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody().returnResult().getResponseHeaders().getLocation();

        URI taskCommentsUri = URI.create(taskUri.toString() + "/comments");

        authenticate("userb@gmail.com");
        client.post().uri(taskCommentsUri)
                .bodyValue(Map.of("text", "I agree"))
                .exchange()
                .expectStatus().isNotFound();

        client.get().uri(taskCommentsUri)
                .exchange()
                .expectStatus().isNotFound();

    }

    @Test
    public void userCanOnlyListItsOwnComments() {

        authenticate("usera@gmail.com");
        URI taskUri = client.post().uri("/v1/tasks")
                .bodyValue(Map.of("title", "Walk with dog"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody().returnResult().getResponseHeaders().getLocation();

        URI commentsUri = URI.create(taskUri.toString() + "/comments");

        client.post().uri(commentsUri)
                .bodyValue(Map.of("text", "I agree"))
                .exchange()
                .expectStatus().isCreated();

        client.get().uri(commentsUri).exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$._embedded.comments[0].text").isEqualTo("I agree");

        authenticate("userb@gmail.com");
        client.get().uri(commentsUri)
                .exchange()
                .expectStatus().isNotFound();

    }

}
