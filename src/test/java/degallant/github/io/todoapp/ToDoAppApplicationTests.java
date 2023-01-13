package degallant.github.io.todoapp;

import com.fasterxml.jackson.databind.ObjectMapper;
import degallant.github.io.todoapp.openid.OpenIdTokenParser;
import degallant.github.io.todoapp.openid.OpenIdUser;
import degallant.github.io.todoapp.user.UserRepository;
import org.flywaydb.core.Flyway;
import org.hamcrest.Matchers;
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
import java.util.*;

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

        authenticateWithToken(response.get("access_token"));

        client.get()
                .uri(userUri)
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
    public void oneUserCantSeeOthersUsersTodos() {

        String todoFromUserA = "Take dog for a walk";
        String todoFromUserB = "Take cat for a walk";

        //create a task for user A
        authenticate("usera@gmail.com");
        URI todoFromUserAUri = client.post().uri("/v1/todo")
                .bodyValue(Map.of("title", todoFromUserA))
                .exchange()
                .expectStatus().isCreated()
                .expectBody().returnResult()
                .getResponseHeaders().getLocation();

        //create a task for user B
        authenticate("userb@gmail.com");
        URI todoFromUserBUri = client.post().uri("/v1/todo")
                .bodyValue(Map.of("title", todoFromUserB))
                .exchange()
                .expectStatus().isCreated()
                .expectBody().returnResult()
                .getResponseHeaders().getLocation();

        //check user A created task and try to see created task from user b
        authenticate("usera@gmail.com");
        client.get().uri(todoFromUserAUri)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.title").isEqualTo(todoFromUserA);
        client.get().uri(todoFromUserBUri)
                .exchange()
                .expectStatus().is5xxServerError();

        //check user B created task and try to see created task from user a
        authenticate("userb@gmail.com");
        client.get().uri(todoFromUserBUri)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.title").isEqualTo(todoFromUserB);
        client.get().uri(todoFromUserAUri)
                .exchange()
                .expectStatus().is5xxServerError();

    }

    @Test
    public void oneUserCanEditOnlyItsTodos() {

        String todoFromUserA = "Take dog for a walk";
        String todoFromUserB = "Take cat for a walk";

        //create a task for user A
        authenticate("usera@gmail.com");
        URI todoFromUserAUri = client.post().uri("/v1/todo")
                .bodyValue(Map.of("title", todoFromUserA))
                .exchange()
                .expectStatus().isCreated()
                .expectBody().returnResult()
                .getResponseHeaders().getLocation();

        //create a task for user B
        authenticate("userb@gmail.com");
        URI todoFromUserBUri = client.post().uri("/v1/todo")
                .bodyValue(Map.of("title", todoFromUserB))
                .exchange()
                .expectStatus().isCreated()
                .expectBody().returnResult()
                .getResponseHeaders().getLocation();

        //confirm that user A can edit its task, while not being able to edits B's
        authenticate("usera@gmail.com");
        client.patch().uri(todoFromUserAUri)
                .bodyValue(Map.of("complete", true))
                .exchange()
                .expectStatus().isOk();
        client.patch().uri(todoFromUserBUri)
                .bodyValue(Map.of("complete", true))
                .exchange()
                .expectStatus().is5xxServerError();

        //confirm that user B can edit its task, while not being able to edits A's
        authenticate("userb@gmail.com");
        client.patch().uri(todoFromUserBUri)
                .bodyValue(Map.of("complete", true))
                .exchange()
                .expectStatus().isOk();
        client.patch().uri(todoFromUserAUri)
                .bodyValue(Map.of("complete", true))
                .exchange()
                .expectStatus().is5xxServerError();

    }

    @Test
    public void aUserCanOnlyListItsTodos() throws IOException {

        List<String> userATodos = List.of("Take dog for a walk", "Go get milk", "Study for test");
        List<String> userBTodos = List.of("Take cat for a walk", "Play games");

        authenticate("usera@gmail.com");
        createTodosForUser(userATodos);
        authenticate("userb@gmail.com");
        createTodosForUser(userBTodos);

        authenticate("usera@gmail.com");
        client.get().uri("/v1/todo").exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(3)
                .jsonPath("$[?(@.title == '%s')]", userATodos.get(0)).exists()
                .jsonPath("$[?(@.title == '%s')]", userATodos.get(1)).exists()
                .jsonPath("$[?(@.title == '%s')]", userATodos.get(2)).exists()
                .jsonPath("$[?(@.title == '%s')]", userBTodos.get(0)).doesNotExist();

        authenticate("userb@gmail.com");
        client.get().uri("/v1/todo").exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(2)
                .jsonPath("$[?(@.title == '%s')]", userBTodos.get(0)).exists()
                .jsonPath("$[?(@.title == '%s')]", userBTodos.get(1)).exists()
                .jsonPath("$[?(@.title == '%s')]", userATodos.get(0)).doesNotExist();

    }

    @Test
    public void aUserCanOnlySeeItsOwnTags() {

        authenticate("usera@gmail.com");
        URI userATag = client.post().uri("/v1/tag")
                .bodyValue(Map.of("name", "house"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody().returnResult()
                .getResponseHeaders().getLocation();

        client.get().uri(userATag).exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.name").isEqualTo("house");

        authenticate("userb@gmail.com");
        client.get().uri(userATag).exchange()
                .expectStatus().is5xxServerError();

    }

    @Test
    public void createsAToDoToComplete() throws IOException {

        authenticate();

        var title = "Take the dog for a walk";

        client.post().uri("/v1/todo")
                .bodyValue(Map.of("title", title))
                .exchange()
                .expectStatus().isCreated();

        client.get().uri("/v1/todo")
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
    public void createATag() throws IOException {

        authenticate();

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
    public void createATodoFullOfDetails() throws IOException {

        authenticate();

        var title = "Take the dog for a walk";
        var description = "This is very important, dog needs to walk or it will not behave";
        var dueDate = "2023-01-01T12:50:29.790511-04:00";
        var priority = "P3";
        var tags = makeTags("daily", "home", "pet");
        var projectId = makeProject("daily tasks");

        URI todoURI = client.post().uri("/v1/todo")
                .bodyValue(Map.of(
                        "title", title,
                        "description", description,
                        "due_date", dueDate,
                        "priority", priority,
                        "tags", tags,
                        "project",projectId
                ))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .returnResult()
                .getResponseHeaders()
                .getLocation();

        client.get().uri(todoURI)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .consumeWith(System.out::println)
                .jsonPath("$.title").isEqualTo(title)
                .jsonPath("$.description").isEqualTo(description)
                .jsonPath("$.due_date").isEqualTo(dueDate)
                .jsonPath("$.priority").isEqualTo(priority)
                .jsonPath("$.project").value(Matchers.containsString(projectId))
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
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .returnResult()
                .getResponseHeaders()
                .getLocation();

        client.get().uri(todoUri)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.title").isEqualTo(todoTitle)
                .jsonPath("$.children[0]").isEqualTo(subTaskUri.toString());

        client.get().uri(subTaskUri)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.title").isEqualTo(subtaskTitle)
                .jsonPath("$.parent").isEqualTo(todoUri.toString());

    }

    @Test
    public void userCanAddCommentToATodo() {

        var comment = "We have to finish this soon";
        authenticate();

        URI todoUri = client.post().uri("/v1/todo")
                .bodyValue(Map.of("title", "Walk with dog"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody().returnResult().getResponseHeaders().getLocation();

        URI commentsUri = URI.create(todoUri.toString() + "/comments");

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
    public void userCanOnlyCommentOnItsTodos() {

        authenticate("usera@gmail.com");
        URI todoUri = client.post().uri("/v1/todo")
                .bodyValue(Map.of("title", "Walk with dog"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody().returnResult().getResponseHeaders().getLocation();

        URI commentsUri = URI.create(todoUri.toString() + "/comments");

        authenticate("userb@gmail.com");
        client.post().uri(commentsUri)
                .bodyValue(Map.of("comment", "I agree"))
                .exchange()
                .expectStatus().is5xxServerError();

        client.get().uri(commentsUri)
                .exchange()
                .expectStatus().is5xxServerError();

    }

    @Test
    public void addATodoToAProject() {

        authenticate();

        URI projectURI = client.post().uri("/v1/projects")
                .bodyValue(Map.of("title", "House summer cleanup"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody().returnResult().getResponseHeaders().getLocation();

        String[] parts = projectURI.toString().split("/");
        String projectId = parts[parts.length - 1];

        URI todoUri = client.post().uri("/v1/todo")
                .bodyValue(Map.of("title", "Clean up attic"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody().returnResult().getResponseHeaders().getLocation();

        client.patch().uri(todoUri)
                .bodyValue(Map.of("project_id", projectId))
                .exchange()
                .expectStatus().isOk();

        client.get().uri(todoUri)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.project").isEqualTo(projectURI.toString());

    }

    @Test
    public void userCanOnlySeeItsProjects() {

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

    //todo move everything from todo to task

    //todo separate test file

    //todo define a standard for yourself for response and endpoint naming

    //todo test fail cases

    //todo paginate anything that returns a list

    //todo make refresh token works

    //todo add test for invalid tokens

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

    private void authenticate() {
        authenticate("email@gmail.com");
    }

    private void authenticate(String email) {
        String name = "Jhon Doe";
        String profileUrl = "https://google.com/profile/903jfiwfiwoe";
        String token = makeTokenFor(email, name, profileUrl);
        EntityExchangeResult<byte[]> result = client.post().uri("/v1/auth")
                .bodyValue(Map.of("open_id_token", token))
                .exchange()
                .expectBody().returnResult();
        try {
            Map<String, String> response = mapper.readValue(result.getResponseBodyContent(), Map.class);
            authenticateWithToken(response.get("access_token"));
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    private void createTodosForUser(List<String> todos) {
        for (String todo : todos) {
            client.post().uri("/v1/todo")
                    .bodyValue(Map.of("title", todo))
                    .exchange()
                    .expectStatus().isCreated();
        }
    }

    private void authenticateWithToken(String accessToken) {
        client = client.mutateWith((builder, httpHandlerBuilder, connector) -> {
            builder.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
        });
    }

    private String makeProject(String title) {
        URI uri = client.post().uri("/v1/projects")
                .bodyValue(Map.of("title", title))
                .exchange()
                .expectStatus().isCreated()
                .expectBody().returnResult()
                .getResponseHeaders().getLocation();

        String[] parts = uri.toString().split("/");

        return parts[parts.length - 1];
    }

    private void show() {
        client = client.mutateWith((builder, httpHandlerBuilder, connector) -> {
            builder.entityExchangeResultConsumer(result -> {
                URI uri = result.getUrl();
                System.out.println("Response from " + uri + ": " + new String(result.getResponseBodyContent()));
            });
        });
    }

}
