package degallant.github.io.todoapp;

import org.junit.jupiter.api.Test;

import java.util.Map;

public class TagsTests extends IntegrationTest {

    @Test
    public void createATag() {

        authenticate();

        var tag = "house";

        client.post().uri("/v1/tags")
                .bodyValue(Map.of("name", tag))
                .exchange()
                .expectStatus().isCreated()
                .expectBody().returnResult().getResponseHeaders()
                .getLocation();

        client.get().uri("/v1/tags")
                .exchange()
                .expectBody()
                .jsonPath("$._embedded.tags[0].name").isEqualTo(tag);

    }

    @Test
    public void aUserCanOnlySeeItsOwnTags() {

        authenticate("usera@gmail.com");
        client.post().uri("/v1/tags")
                .bodyValue(Map.of("name", "house"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody().returnResult()
                .getResponseHeaders().getLocation();

        client.get().uri("/v1/tags").exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$._embedded.tags[0].name").isEqualTo("house");

        authenticate("userb@gmail.com");
        client.get().uri("/v1/tags").exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$._embedded.tags").isEmpty();

    }

}
