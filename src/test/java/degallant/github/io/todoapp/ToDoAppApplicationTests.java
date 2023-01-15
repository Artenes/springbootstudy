package degallant.github.io.todoapp;

import com.fasterxml.jackson.databind.ObjectMapper;
import degallant.github.io.todoapp.openid.OpenIdTokenParser;
import degallant.github.io.todoapp.openid.OpenIdUser;
import degallant.github.io.todoapp.users.UsersRepository;
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
    private UsersRepository repository;

    @BeforeEach
    public void setUp() {
        flyway.migrate();
    }

    @Test
    public void oneUserCantSeeOthersUserstasks() {

        String taskFromUserA = "Take dog for a walk";
        String taskFromUserB = "Take cat for a walk";

        //create a task for user A
        authenticate("usera@gmail.com");
        URI taskFromUserAUri = client.post().uri("/v1/tasks")
                .bodyValue(Map.of("title", taskFromUserA))
                .exchange()
                .expectStatus().isCreated()
                .expectBody().returnResult()
                .getResponseHeaders().getLocation();

        //create a task for user B
        authenticate("userb@gmail.com");
        URI taskFromUserBUri = client.post().uri("/v1/tasks")
                .bodyValue(Map.of("title", taskFromUserB))
                .exchange()
                .expectStatus().isCreated()
                .expectBody().returnResult()
                .getResponseHeaders().getLocation();

        //check user A created task and try to see created task from user b
        authenticate("usera@gmail.com");
        client.get().uri(taskFromUserAUri)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.title").isEqualTo(taskFromUserA);
        client.get().uri(taskFromUserBUri)
                .exchange()
                .expectStatus().is5xxServerError();

        //check user B created task and try to see created task from user a
        authenticate("userb@gmail.com");
        client.get().uri(taskFromUserBUri)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.title").isEqualTo(taskFromUserB);
        client.get().uri(taskFromUserAUri)
                .exchange()
                .expectStatus().is5xxServerError();

    }

    @Test
    public void oneUserCanEditOnlyItstasks() {

        String taskFromUserA = "Take dog for a walk";
        String taskFromUserB = "Take cat for a walk";

        //create a task for user A
        authenticate("usera@gmail.com");
        URI taskFromUserAUri = client.post().uri("/v1/tasks")
                .bodyValue(Map.of("title", taskFromUserA))
                .exchange()
                .expectStatus().isCreated()
                .expectBody().returnResult()
                .getResponseHeaders().getLocation();

        //create a task for user B
        authenticate("userb@gmail.com");
        URI taskFromUserBUri = client.post().uri("/v1/tasks")
                .bodyValue(Map.of("title", taskFromUserB))
                .exchange()
                .expectStatus().isCreated()
                .expectBody().returnResult()
                .getResponseHeaders().getLocation();

        //confirm that user A can edit its task, while not being able to edits B's
        authenticate("usera@gmail.com");
        client.patch().uri(taskFromUserAUri)
                .bodyValue(Map.of("complete", true))
                .exchange()
                .expectStatus().isOk();
        client.patch().uri(taskFromUserBUri)
                .bodyValue(Map.of("complete", true))
                .exchange()
                .expectStatus().is5xxServerError();

        //confirm that user B can edit its task, while not being able to edits A's
        authenticate("userb@gmail.com");
        client.patch().uri(taskFromUserBUri)
                .bodyValue(Map.of("complete", true))
                .exchange()
                .expectStatus().isOk();
        client.patch().uri(taskFromUserAUri)
                .bodyValue(Map.of("complete", true))
                .exchange()
                .expectStatus().is5xxServerError();

    }

    @Test
    public void aUserCanOnlyListItstasks() throws IOException {

        List<String> userAtasks = List.of("Take dog for a walk", "Go get milk", "Study for test");
        List<String> userBtasks = List.of("Take cat for a walk", "Play games");

        authenticate("usera@gmail.com");
        createtasksForUser(userAtasks);
        authenticate("userb@gmail.com");
        createtasksForUser(userBtasks);

        authenticate("usera@gmail.com");
        client.get().uri("/v1/tasks").exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(3)
                .jsonPath("$[?(@.title == '%s')]", userAtasks.get(0)).exists()
                .jsonPath("$[?(@.title == '%s')]", userAtasks.get(1)).exists()
                .jsonPath("$[?(@.title == '%s')]", userAtasks.get(2)).exists()
                .jsonPath("$[?(@.title == '%s')]", userBtasks.get(0)).doesNotExist();

        authenticate("userb@gmail.com");
        client.get().uri("/v1/tasks").exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(2)
                .jsonPath("$[?(@.title == '%s')]", userBtasks.get(0)).exists()
                .jsonPath("$[?(@.title == '%s')]", userBtasks.get(1)).exists()
                .jsonPath("$[?(@.title == '%s')]", userAtasks.get(0)).doesNotExist();

    }

    @Test
    public void aUserCanOnlySeeItsOwnTags() {

        authenticate("usera@gmail.com");
        URI userATag = client.post().uri("/v1/tags")
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
    public void createsAtaskToComplete() throws IOException {

        authenticate();

        var title = "Take the dog for a walk";

        client.post().uri("/v1/tasks")
                .bodyValue(Map.of("title", title))
                .exchange()
                .expectStatus().isCreated();

        client.get().uri("/v1/tasks")
                .exchange()
                .expectBody()
                .jsonPath("$[0].title").isEqualTo(title)
                .jsonPath("$[0].complete").isEqualTo(false)
                .consumeWith(System.out::println);

    }

    @Test
    public void createsAndCompletetask() throws IOException {

        authenticate();

        var title = "Take the dog for a walk";

        URI taskURI = client.post().uri("/v1/tasks")
                .bodyValue(Map.of("title", title))
                .exchange()
                .expectBody()
                .returnResult()
                .getResponseHeaders()
                .getLocation();

        client.patch().uri(taskURI)
                .bodyValue(Map.of("complete", true))
                .exchange();

        client.get().uri("/v1/tasks")
                .exchange()
                .expectBody()
                .jsonPath("$[0].title").isEqualTo(title)
                .jsonPath("$[0].complete").isEqualTo(true);

    }

    @Test
    public void createATag() throws IOException {

        authenticate();

        var tag = "house";

        URI tagUri = client.post().uri("/v1/tags")
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
    public void createAtaskFullOfDetails() throws IOException {

        authenticate();

        var title = "Take the dog for a walk";
        var description = "This is very important, dog needs to walk or it will not behave";
        var dueDate = "2023-01-01T12:50:29.790511-04:00";
        var priority = "P3";
        var tags = makeTags("daily", "home", "pet");
        var projectId = makeProject("daily tasks");

        URI taskURI = client.post().uri("/v1/tasks")
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

        client.get().uri(taskURI)
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

        var taskTitle = "Buy some bread";
        URI taskUri = client.post().uri("/v1/tasks")
                .bodyValue(Map.of("title", taskTitle))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .returnResult()
                .getResponseHeaders()
                .getLocation();

        String[] parts = taskUri.getPath().split("/");
        String parentUuid = parts[parts.length - 1];

        var subtaskTitle = "Go to store";
        URI subTaskUri = client.post().uri("/v1/tasks")
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

        client.get().uri(taskUri)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.title").isEqualTo(taskTitle)
                .jsonPath("$.children[0]").isEqualTo(subTaskUri.toString());

        client.get().uri(subTaskUri)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.title").isEqualTo(subtaskTitle)
                .jsonPath("$.parent").isEqualTo(taskUri.toString());

    }

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

    @Test
    public void addAtaskToAProject() {

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

    @AfterEach
    public void tearDown() {
        flyway.clean();
    }

    private Set<String> makeTags(String... names) {
        Set<String> uuids = new HashSet<>();
        for (String name : names) {
            URI uri = client.post().uri("/v1/tags")
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

    private void createtasksForUser(List<String> tasks) {
        for (String task : tasks) {
            client.post().uri("/v1/tasks")
                    .bodyValue(Map.of("title", task))
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
