package degallant.github.io.todoapp;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.Map;

/**
 * @noinspection ConstantConditions
 */
public class CommentsTests extends IntegrationTest {

    @Test
    public void userCanAddCommentToAtask() {

        var comment = "We have to finish this soon";
        authenticate();

        URI taskUri = client.post().uri("/v1/tasks")
                .bodyValue(Map.of("title", "Walk with dog"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody().returnResult().getResponseHeaders().getLocation();

        URI commentsUri = URI.create(taskUri.toString() + "/comments");

        client.post().uri(commentsUri)
                .bodyValue(Map.of("text", comment))
                .exchange()
                .expectStatus().isCreated();

        client.get().uri(commentsUri)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].text").isEqualTo(comment);

    }

    @Test
    public void userCanOnlyCommentOnItstasks() {

        authenticate("usera@gmail.com");
        URI taskUri = client.post().uri("/v1/tasks")
                .bodyValue(Map.of("title", "Walk with dog"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody().returnResult().getResponseHeaders().getLocation();

        URI commentsUri = URI.create(taskUri.toString() + "/comments");

        authenticate("userb@gmail.com");
        client.post().uri(commentsUri)
                .bodyValue(Map.of("comment", "I agree"))
                .exchange()
                .expectStatus().is5xxServerError();

        client.get().uri(commentsUri)
                .exchange()
                .expectStatus().is5xxServerError();

    }

}
