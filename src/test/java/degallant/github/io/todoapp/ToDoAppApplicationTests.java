package degallant.github.io.todoapp;

import com.fasterxml.jackson.databind.ObjectMapper;
import degallant.github.io.todoapp.openid.OpenIdTokenParser;
import degallant.github.io.todoapp.openid.OpenIdUser;
import degallant.github.io.todoapp.user.UserRepository;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.io.IOException;
import java.net.URI;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * @noinspection ALL
 */
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient(timeout = "36000")
class ToDoAppApplicationTests {

    @Autowired
    private WebTestClient client;

    @Autowired
    private Flyway flyway;

    @MockBean
    private OpenIdTokenParser openIdTokenParser;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private UserRepository repository;

    private String accessToken;

    @BeforeEach
    public void setUp() {
        flyway.migrate();
    }

    @Test
    public void registerNewUser() throws IOException {

        String email = "email@gmail.com";
        String name = "Jhon Doe";
        String profileUrl = "https://google.com/profile/903jfiwfiwoe";

        String token = makeTokenFor(email, name, profileUrl);

        EntityExchangeResult<byte[]> result = client.post().uri("/v1/auth")
                .bodyValue(Map.of("open_id_token", token))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.access_token").exists()
                .jsonPath("$.refresh_token").exists()
                .returnResult();

        URI userUri = result.getResponseHeaders().getLocation();
        Map<String, String> response = mapper.readValue(result.getResponseBodyContent(), Map.class);

        authenticate(response.get("access_token"));

        client.get()
                .uri(userUri)
                .headers(this::authenticateRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.email").isEqualTo(email)
                .jsonPath("$.name").isEqualTo(name)
                .jsonPath("$.picture_url").isEqualTo(profileUrl);

    }

    @Test
    public void loginExistingUser() {

        String email = "email@gmail.com";
        String name = "Jhon Doe";
        String profileUrl = "https://google.com/profile/903jfiwfiwoe";

        String token = makeTokenFor(email, name, profileUrl);

        client.post().uri("/v1/auth")
                .bodyValue(Map.of("open_id_token", token))
                .exchange()
                .expectStatus().isCreated();

        client.post().uri("/v1/auth")
                .bodyValue(Map.of("open_id_token", token))
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.access_token").exists();

    }

    @Test
    public void createsAToDoToComplete() throws IOException {

        authenticate();

        var title = "Take the dog for a walk";

        client.post().uri("/v1/todo")
                .bodyValue(Map.of("title", title))
                .headers(this::authenticateRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectBody();

        client.get().uri("/v1/todo")
                .headers(this::authenticateRequest)
                .exchange()
                .expectBody()
                .jsonPath("$[0].title").isEqualTo(title)
                .jsonPath("$[0].complete").isEqualTo(false)
                .consumeWith(System.out::println);

    }

    @Test
    public void createsAndCompleteTodo() throws IOException {

        authenticate();

        var title = "Take the dog for a walk";

        URI todoURI = client.post().uri("/v1/todo")
                .bodyValue(Map.of("title", title))
                .headers(this::authenticateRequest)
                .exchange()
                .expectBody()
                .returnResult()
                .getResponseHeaders()
                .getLocation();

        client.patch().uri(todoURI)
                .bodyValue(Map.of("complete", true))
                .headers(this::authenticateRequest)
                .exchange();

        client.get().uri("/v1/todo")
                .headers(this::authenticateRequest)
                .exchange()
                .expectBody()
                .jsonPath("$[0].title").isEqualTo(title)
                .jsonPath("$[0].complete").isEqualTo(true);

    }

    @Test
    public void createATag() throws IOException {

        authenticate();

        var tag = "house";

        URI tagUri = client.post().uri("/v1/tag")
                .bodyValue(Map.of("name", tag))
                .headers(this::authenticateRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectBody().returnResult().getResponseHeaders()
                .getLocation();

        client.get().uri(tagUri)
                .headers(this::authenticateRequest)
                .exchange()
                .expectBody()
                .jsonPath("$.name").isEqualTo(tag);

    }

    @Test
    public void createATodoFullOfDetails() throws IOException {

        authenticate();

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
                .headers(this::authenticateRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .returnResult()
                .getResponseHeaders()
                .getLocation();

        client.get().uri(todoURI)
                .headers(this::authenticateRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .consumeWith(System.out::println)
                .jsonPath("$.title").isEqualTo(title)
                .jsonPath("$.description").isEqualTo(description)
                .jsonPath("$.due_date").isEqualTo(dueDate)
                .jsonPath("$.priority").isEqualTo(priority)
                .jsonPath("$.tags[?(@.name == 'daily')]").exists()
                .jsonPath("$.tags[?(@.name == 'home')]").exists()
                .jsonPath("$.tags[?(@.name == 'pet')]").exists()
                .jsonPath("$.children").isEmpty()
                .jsonPath("$.parent").isEmpty();

    }

    @Test
    public void createASubTask() throws IOException {

        authenticate();

        var todoTitle = "Buy some bread";
        URI todoUri = client.post().uri("/v1/todo")
                .bodyValue(Map.of("title", todoTitle))
                .headers(this::authenticateRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .returnResult()
                .getResponseHeaders()
                .getLocation();

        String[] parts = todoUri.getPath().split("/");
        String parentUuid = parts[parts.length - 1];

        var subtaskTitle = "Go to store";
        URI subTaskUri = client.post().uri("/v1/todo")
                .bodyValue(Map.of(
                        "title", subtaskTitle,
                        "parent", parentUuid
                ))
                .headers(this::authenticateRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .returnResult()
                .getResponseHeaders()
                .getLocation();

        client.get().uri(todoUri)
                .headers(this::authenticateRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.title").isEqualTo(todoTitle)
                .jsonPath("$.children[0]").isEqualTo(subTaskUri.toString());

        client.get().uri(subTaskUri)
                .headers(this::authenticateRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.title").isEqualTo(subtaskTitle)
                .jsonPath("$.parent").isEqualTo(todoUri.toString());

    }

    //todo add support to users

    //todo add token-based authentication

    //todo add support to comments

    //todo add support to projects

    //todo test fail cases

    //todo paginate anything that returns a list

    //about date time in java https://reflectoring.io/spring-timezones/

    //retardedly, you can't have enums in the database and bring them to hibernate, maybe this can help: https://docs.jboss.org/hibernate/orm/6.1/userguide/html_single/Hibernate_User_Guide.html#basic-enums

    //when creating queries in a repository, by default it uses JPQL

    @AfterEach
    public void tearDown() {
        flyway.clean();
    }

    private Set<String> makeTags(String... names) {
        Set<String> uuids = new HashSet<>();
        for (String name : names) {
            URI uri = client.post().uri("/v1/tag")
                    .bodyValue(Map.of("name", name))
                    .headers(this::authenticateRequest)
                    .exchange()
                    .expectStatus().isCreated()
                    .expectBody().returnResult().getResponseHeaders()
                    .getLocation();
            String[] parts = uri.getPath().split("/");
            uuids.add(parts[parts.length - 1]);
        }
        return uuids;
    }

    private String makeTokenFor(String email, String name, String profileUrl) {
        //a dummy token for test purposes
        String token = email + name + profileUrl;
        when(openIdTokenParser.extract(eq(token))).thenReturn(new OpenIdUser(email, name, profileUrl));
        return token;
    }

    private void authenticate() throws IOException {
        String email = "email@gmail.com";
        String name = "Jhon Doe";
        String profileUrl = "https://google.com/profile/903jfiwfiwoe";
        String token = makeTokenFor(email, name, profileUrl);
        EntityExchangeResult<byte[]> result = client.post().uri("/v1/auth")
                .bodyValue(Map.of("open_id_token", token))
                .exchange()
                .expectStatus().isCreated()
                .expectBody().returnResult();
        URI userUri = result.getResponseHeaders().getLocation();
        Map<String, String> response = mapper.readValue(result.getResponseBodyContent(), Map.class);
        accessToken = response.get("access_token");
    }

    private void authenticate(String accessToken) {
        this.accessToken = accessToken;
    }

    private void authenticateRequest(HttpHeaders headers) {
        if (accessToken == null || accessToken.isBlank()) {
            return;
        }
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
    }

    private void printBody(EntityExchangeResult<byte[]> body) {
        System.out.println(body);
    }

}
