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

        client.post().uri("/v1/todo")
                .bodyValue(new TodoDto.Create("Take the dog for a walk"))
                .exchange();

        client.get().uri("/v1/todo")
                .exchange()
                .expectBody()
                .consumeWith(System.out::println)
                .jsonPath("$[0].description").isEqualTo("Take the dog for a walk")
                .jsonPath("$[0].complete").isEqualTo(false);

    }

    @Test
    public void createsAndCompleteTodo() {
        URI todoURI = client.post().uri("/v1/todo")
                .bodyValue(new TodoDto.Create("Take the dog for a walk"))
                .exchange()
                .expectBody()
                .returnResult()
                .getResponseHeaders()
                .getLocation();

        client.patch().uri(todoURI.toString())
                .bodyValue(new TodoDto.PatchComplete(true))
                .exchange();

        client.get().uri("/v1/todo")
                .exchange()
                .expectBody()
                .jsonPath("$[0].description").isEqualTo("Take the dog for a walk")
                .jsonPath("$[0].complete").isEqualTo(true);
    }

    //todo add support to title and description

    //todo add support to due date

    //todo add support to priority

    //todo add support to labels

    //todo add support to subtasks

    //todo add support to users

    //todo add support to comments

    //todo add support to buckets

    @AfterEach
    public void tearDown() {
        flyway.clean();
    }

}
