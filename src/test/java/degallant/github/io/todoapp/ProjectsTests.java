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

    //todo separate test file

    //todo define a standard for yourself for response and endpoint naming

    //todo test fail cases

    //todo paginate anything that returns a list

    //todo make refresh token works

    //todo add test for invalid tokens

    //todo better organize routes in one place

    //todo extract authenticate user for easier access

    //todo add proper exception handling

    //todo deal with internationalization

    //todo deal with users roles

    //todo check about caching - Cache-Control header

    //todo deal with rate limit

    //todo deal with logginh and monitoring

    //todo check about graphql

    //todo get back to OpenAPI/Swagger

    //todo check about CORS

    //about date time in java https://reflectoring.io/spring-timezones/

    //retardedly, you can't have enums in the database and bring them to hibernate, maybe this can help: https://docs.jboss.org/hibernate/orm/6.1/userguide/html_single/Hibernate_User_Guide.html#basic-enums

    //when creating queries in a repository, by default it uses JPQL

    //top 10 security issues with rest apis https://github.com/OWASP/API-Security

    //api versioning https://www.troyhunt.com/your-api-versioning-is-wrong-which-is/

    //HATEOAS https://stateless.co/hal_specification.html https://restfulapi.net/hateoas/

    //API paging https://www.mixmax.com/engineering/api-paging-built-the-right-way

}
