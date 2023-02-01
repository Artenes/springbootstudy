package degallant.github.io.todoapp;

import degallant.github.io.todoapp.common.IntegrationTest;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.Map;

/** @noinspection ConstantConditions*/
public class TagsTests extends IntegrationTest {

    @Test
    public void failsCreationWithEmptyBody() {
        authenticate();
        client.post().uri("/v1/tags")
                .bodyValue(Map.of())
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    public void createATag() {

        authenticate();

        var tag = "house";

        URI uri = client.post().uri("/v1/tags")
                .bodyValue(Map.of("name", tag))
                .exchange()
                .expectStatus().isCreated()
                .expectBody().returnResult()
                .getResponseHeaders().getLocation();

        client.get().uri(uri)
                .exchange()
                .expectBody()
                .jsonPath("$.name").isEqualTo(tag);

    }

    @Test
    public void aUserCanOnlyListItsOwnTags() {

        authenticate("usera@gmail.com");
        client.post().uri("/v1/tags")
                .bodyValue(Map.of("name", "house"))
                .exchange()
                .expectStatus().isCreated();

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

    @Test
    public void aUserCanOnlySeeTheDetailsOfItsOwnTags() {

        authenticate("usera@gmail.com");
        URI uri = client.post().uri("/v1/tags")
                .bodyValue(Map.of("name", "house"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody().returnResult()
                .getResponseHeaders().getLocation();

        client.get().uri(uri).exchange()
                .expectStatus().isOk();

        authenticate("userb@gmail.com");
        client.get().uri(uri).exchange()
                .expectStatus().isNotFound();

    }

}
