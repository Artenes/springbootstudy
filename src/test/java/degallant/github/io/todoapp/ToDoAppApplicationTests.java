package degallant.github.io.todoapp;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.net.URI;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @noinspection ConstantConditions
 */
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ToDoAppApplicationTests {

    @Autowired
    private WebTestClient client;

    @Autowired
    private Flyway flyway;

    @BeforeEach
    public void setUp() {
        flyway.migrate();
    }

    @Test
    public void createsAToDoToComplete() {

        var title = "Take the dog for a walk";

        client.post().uri("/v1/todo")
                .bodyValue(Map.of("title", title))
                .exchange()
                .expectStatus().isCreated()
                .expectBody();

        client.get().uri("/v1/todo")
                .exchange()
                .expectBody()
                .jsonPath("$[0].title").isEqualTo(title)
                .jsonPath("$[0].complete").isEqualTo(false)
                .consumeWith(System.out::println);

    }

    @Test
    public void createsAndCompleteTodo() {

        var title = "Take the dog for a walk";

        URI todoURI = client.post().uri("/v1/todo")
                .bodyValue(Map.of("title", title))
                .exchange()
                .expectBody()
                .returnResult()
                .getResponseHeaders()
                .getLocation();

        client.patch().uri(todoURI)
                .bodyValue(Map.of("complete", true))
                .exchange();

        client.get().uri("/v1/todo")
                .exchange()
                .expectBody()
                .jsonPath("$[0].title").isEqualTo(title)
                .jsonPath("$[0].complete").isEqualTo(true);

    }

    @Test
    public void createATag() {

        var tag = "house";

        URI tagUri = client.post().uri("/v1/tag")
                .bodyValue(Map.of("name", tag))
                .exchange()
                .expectStatus().isCreated()
                .expectBody().returnResult().getResponseHeaders()
                .getLocation();

        client.get().uri(tagUri)
                .exchange()
                .expectBody()
                .jsonPath("$.name").isEqualTo(tag);

    }

    @Test
    public void createATodoFullOfDetails() {

        var title = "Take the dog for a walk";
        var description = "This is very important, dog needs to walk or it will not behave";
        var dueDate = "2023-01-01T12:50:29.790511-04:00";
        var priority = "P3";
        var tags = makeTags("daily", "home", "pet");

        URI todoURI = client.post().uri("/v1/todo")
                .bodyValue(Map.of(
                        "title", title,
                        "description", description,
                        "due_date", dueDate,
                        "priority", priority,
                        "tags", tags
                ))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .returnResult()
                .getResponseHeaders()
                .getLocation();

        client.get().uri(todoURI).exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.title").isEqualTo(title)
                .jsonPath("$.description").isEqualTo(description)
                .jsonPath("$.due_date").isEqualTo(dueDate)
                .jsonPath("$.priority").isEqualTo(priority)
                .jsonPath("$.tags[?(@.name == 'daily')]").exists()
                .jsonPath("$.tags[?(@.name == 'home')]").exists()
                .jsonPath("$.tags[?(@.name == 'pet')]").exists();

    }

    //todo add support to subtasks

    //todo add support to users

    //todo add support to comments

    //todo add support to projects

    //todo test fail cases

    //todo paginate anything that returns a list

    //about date time in java https://reflectoring.io/spring-timezones/

    //retardedly, you can't have enums in the database and bring them to hibernate, maybe this can help: https://docs.jboss.org/hibernate/orm/6.1/userguide/html_single/Hibernate_User_Guide.html#basic-enums

    @AfterEach
    public void tearDown() {
        flyway.clean();
    }

    private Set<String> makeTags(String... names) {
        Set<String> uuids = new HashSet<>();
        for (String name : names) {
            URI uri = client.post().uri("/v1/tag")
                    .bodyValue(Map.of("name", name))
                    .exchange()
                    .expectBody().returnResult().getResponseHeaders()
                    .getLocation();
            String[] parts = uri.getPath().split("/");
            uuids.add(parts[parts.length - 1]);
        }
        return uuids;
    }

}
