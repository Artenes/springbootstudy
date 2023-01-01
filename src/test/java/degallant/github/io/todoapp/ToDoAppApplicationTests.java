package degallant.github.io.todoapp;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.net.URI;
import java.util.Map;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ToDoAppApplicationTests {

    @Autowired
    private WebTestClient client;

    @Autowired
    private Flyway flyway;

    @LocalServerPort
    private int port;

    private URI root;

    @BeforeEach
    public void setUp() {
        flyway.migrate();
        root = URI.create("http://localhost:" + port);
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

        client.patch().uri(root.resolve(todoURI))
                .bodyValue(Map.of("complete", true))
                .exchange();

        client.get().uri("/v1/todo")
                .exchange()
                .expectBody()
                .jsonPath("$[0].title").isEqualTo(title)
                .jsonPath("$[0].complete").isEqualTo(true);

    }

    @Test
    public void createATodoFullOfDetails() {

        var title = "Take the dog for a walk";
        var description = "This is very important, dog needs to walk or it will not behave";
        var dueDate = "2023-01-01T12:50:29.790511-04:00";

        URI todoURI = client.post().uri("/v1/todo")
                .bodyValue(Map.of(
                        "title", title,
                        "description", description,
                        "due_date", dueDate
                ))
                .exchange()
                .expectBody()
                .returnResult()
                .getResponseHeaders()
                .getLocation();

        client.get().uri(root.resolve(todoURI)).exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.title").isEqualTo(title)
                .jsonPath("$.description").isEqualTo(description)
                .jsonPath("$.due_date").isEqualTo(dueDate);

    }

    //todo add support to priority

    //todo add support to labels

    //todo add support to subtasks

    //todo add support to users

    //todo add support to comments

    //todo add support to projects

    //todo test fail cases

    //about date time in java https://reflectoring.io/spring-timezones/

    //retardedly, you can't have enums in the database and bring them to hibernate, maybe this can help: https://docs.jboss.org/hibernate/orm/6.1/userguide/html_single/Hibernate_User_Guide.html#basic-enums

    @AfterEach
    public void tearDown() {
        flyway.clean();
    }

}
