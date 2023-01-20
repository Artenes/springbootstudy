package degallant.github.io.todoapp;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.Map;

/**
 * @noinspection ConstantConditions
 */
class ProjectsTests extends IntegrationTest {

    @Test
    public void addATaskToAProject() {

        authenticate();

        URI projectURI = client.post().uri("/v1/projects")
                .bodyValue(Map.of("title", "House summer cleanup"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody().returnResult().getResponseHeaders().getLocation();

        String[] parts = projectURI.toString().split("/");
        String projectId = parts[parts.length - 1];

        URI taskUri = client.post().uri("/v1/tasks")
                .bodyValue(Map.of("title", "Clean up attic"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody().returnResult().getResponseHeaders().getLocation();

        client.patch().uri(taskUri)
                .bodyValue(Map.of("project_id", projectId))
                .exchange()
                .expectStatus().isOk();

        client.get().uri(taskUri)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.project").isEqualTo(projectURI.toString());

    }

    @Test
    public void aUserCanOnlySeeItsOwnProjects() {

        authenticate("usera@gmail.com");

        URI projectUri = client.post().uri("/v1/projects")
                .bodyValue(Map.of("title", "Test project"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody().returnResult()
                .getResponseHeaders().getLocation();

        authenticate("userb@gmail.com");

        client.get().uri(projectUri).exchange()
                .expectStatus().is5xxServerError();

    }

    @Test
    public void aUserCanOnlyListItsOwnProjects() {

        authenticate("usera@gmail.com");
        client.post().uri("/v1/projects")
                .bodyValue(Map.of("title", "house"))
                .exchange()
                .expectStatus().isCreated();

        client.get().uri("/v1/projects").exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$._embedded.projects[0].title").isEqualTo("house");

        authenticate("userb@gmail.com");
        client.get().uri("/v1/projects").exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$._embedded.projects").isEmpty();

    }

}
