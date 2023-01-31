package degallant.github.io.todoapp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import degallant.github.io.todoapp.common.Authenticator;
import degallant.github.io.todoapp.common.ClientProxy;
import degallant.github.io.todoapp.common.Request;
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

    protected static final String DEFAULT_USER = "default@gmail.com";

    @Autowired
    protected WebTestClient client;

    @Autowired
    protected Flyway flyway;

    @MockBean
    protected OpenIdTokenParser openIdTokenParser;

    @Autowired
    protected ObjectMapper mapper;

    protected Request request;

    private Authenticator authenticator;

    private ClientProxy proxy;

    @BeforeEach
    public void setUp() {
        proxy = new ClientProxy(client);
        authenticator = new Authenticator(proxy, openIdTokenParser, mapper);
        request = new Request(proxy, authenticator, mapper);
        flyway.migrate();
    }

    @AfterEach
    public void tearDown() {
        flyway.clean();
    }

    protected void authenticate() {
        authenticate(DEFAULT_USER);
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

    protected void show(WebTestClient.ResponseSpec spec) {
        spec.expectBody().consumeWith(System.out::println);
    }

    protected void show(WebTestClient.BodyContentSpec spec) {
        spec.consumeWith(System.out::println);
    }

    protected void show() {
        client = client.mutateWith((builder, httpHandlerBuilder, connector) -> builder.entityExchangeResultConsumer(System.out::println));
    }

    protected String makeTagsAsUser(String email, String... names) {
        Set<String> uuids = new HashSet<>();
        for (String name : names) {
            var tagUri = postAsUser(email, "tags")
                    .bodyValue(Map.of("name", name))
                    .exchange()
                    .expectStatus().isCreated()
                    .expectBody().returnResult()
                    .getResponseHeaders().getLocation();

            String[] parts = tagUri.toString().split("/");
            uuids.add(parts[parts.length - 1]);
        }
        try {
            return mapper.writeValueAsString(uuids);
        } catch (JsonProcessingException exception) {
            throw new RuntimeException(exception);
        }
    }

    protected String makeProjectAsUser(String email, String title) {
        URI uri = postAsUser(email, "projects")
                .bodyValue(Map.of("title", title))
                .exchange()
                .expectStatus().isCreated()
                .expectBody().returnResult()
                .getResponseHeaders().getLocation();

        String[] parts = uri.toString().split("/");

        return parts[parts.length - 1];
    }

    protected String makeTaskAsUser(String email, String title) {
        URI uri = postAsUser(email, "tasks")
                .bodyValue(Map.of("title", title))
                .exchange()
                .expectStatus().isCreated()
                .expectBody().returnResult()
                .getResponseHeaders().getLocation();

        String[] parts = uri.toString().split("/");

        return parts[parts.length - 1];
    }

    protected JsonNode parseResponse(WebTestClient.ResponseSpec statusAssertions) {
        try {
            return mapper.readValue(statusAssertions.expectBody().returnResult().getResponseBodyContent(), JsonNode.class);
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    protected JsonNode parseResponse(byte[] response) {
        try {
            return mapper.readValue(response, JsonNode.class);
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    protected String list(Object... items) {
        try {
            return mapper.writeValueAsString(items);
        } catch (JsonProcessingException exception) {
            throw new RuntimeException(exception);
        }
    }

    protected WebTestClient.RequestBodySpec postAsUser(String email, String path) {
        authenticate(email);
        return client.post().uri("/v1/" + path);
    }

}
