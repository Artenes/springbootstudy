package degallant.github.io.todoapp;

import com.fasterxml.jackson.databind.ObjectMapper;
import degallant.github.io.todoapp.openid.OpenIdTokenParser;
import degallant.github.io.todoapp.openid.OpenIdUser;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * @noinspection ConstantConditions, unchecked
 */
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient(timeout = "36000")
public abstract class IntegrationTest {

    @Autowired
    protected WebTestClient client;

    @Autowired
    protected Flyway flyway;

    @MockBean
    protected OpenIdTokenParser openIdTokenParser;

    @Autowired
    protected ObjectMapper mapper;

    @BeforeEach
    public void setUp() {
        flyway.migrate();
    }

    @AfterEach
    public void tearDown() {
        flyway.clean();
    }

    protected void authenticate() {
        authenticate("email@gmail.com");
    }

    protected void authenticate(String email) {
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

    protected String makeTokenFor(String email, String name, String profileUrl) {
        //a dummy token for test purposes
        String token = email + name + profileUrl;
        when(openIdTokenParser.extract(eq(token))).thenReturn(new OpenIdUser(email, name, profileUrl));
        return token;
    }

    protected void authenticateWithToken(String accessToken) {
        client = client.mutateWith((builder, httpHandlerBuilder, connector)
                -> builder.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken));
    }

    protected void show() {
        client = client.mutateWith((builder, httpHandlerBuilder, connector) -> builder.entityExchangeResultConsumer(System.out::println));
    }

    protected Set<String> makeTags(String... names) {
        Set<String> uuids = new HashSet<>();
        for (String name : names) {
            var tagUri = client.post().uri("/v1/tags")
                    .bodyValue(Map.of("name", name))
                    .exchange()
                    .expectStatus().isCreated()
                    .expectBody().returnResult()
                    .getResponseHeaders().getLocation();

            String[] parts = tagUri.toString().split("/");
            uuids.add(parts[parts.length - 1]);
        }
        return uuids;
    }

    protected String makeProject(String title) {
        URI uri = client.post().uri("/v1/projects")
                .bodyValue(Map.of("title", title))
                .exchange()
                .expectStatus().isCreated()
                .expectBody().returnResult()
                .getResponseHeaders().getLocation();

        String[] parts = uri.toString().split("/");

        return parts[parts.length - 1];
    }

    protected void createTasksForUser(List<String> tasks) {
        for (String task : tasks) {
            client.post().uri("/v1/tasks")
                    .bodyValue(Map.of("title", task))
                    .exchange()
                    .expectStatus().isCreated();
        }
    }

}
