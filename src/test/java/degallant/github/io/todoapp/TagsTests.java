package degallant.github.io.todoapp;

import com.fasterxml.jackson.databind.ObjectMapper;
import degallant.github.io.todoapp.openid.OpenIdTokenParser;
import degallant.github.io.todoapp.openid.OpenIdUser;
import degallant.github.io.todoapp.users.UsersRepository;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * @noinspection ConstantConditions, unchecked
 */
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TagsTests {

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
    public void createATag() throws IOException {

        authenticate();

        var tag = "house";

        client.post().uri("/v1/tags")
                .bodyValue(Map.of("name", tag))
                .exchange()
                .expectStatus().isCreated()
                .expectBody().returnResult().getResponseHeaders()
                .getLocation();

        client.get().uri("/v1/tags")
                .exchange()
                .expectBody()
                .jsonPath("$._embedded.tags[0].name").isEqualTo(tag);

    }

    @Test
    public void aUserCanOnlySeeItsOwnTags() {

        authenticate("usera@gmail.com");
        client.post().uri("/v1/tags")
                .bodyValue(Map.of("name", "house"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody().returnResult()
                .getResponseHeaders().getLocation();

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

    @AfterEach
    public void tearDown() {
        flyway.clean();
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

    private void authenticateWithToken(String accessToken) {
        client = client.mutateWith((builder, httpHandlerBuilder, connector) -> {
            builder.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
        });
    }

    private String makeTokenFor(String email, String name, String profileUrl) {
        //a dummy token for test purposes
        String token = email + name + profileUrl;
        when(openIdTokenParser.extract(eq(token))).thenReturn(new OpenIdUser(email, name, profileUrl));
        return token;
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
