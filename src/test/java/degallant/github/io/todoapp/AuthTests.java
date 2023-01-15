package degallant.github.io.todoapp;

import com.fasterxml.jackson.databind.ObjectMapper;
import degallant.github.io.todoapp.openid.OpenIdTokenParser;
import degallant.github.io.todoapp.openid.OpenIdUser;
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

/** @noinspection unchecked, ConstantConditions */
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AuthTests {

    @Autowired
    private WebTestClient client;

    @Autowired
    private Flyway flyway;

    @MockBean
    private OpenIdTokenParser openIdTokenParser;

    @Autowired
    private ObjectMapper mapper;

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
                .jsonPath("$.picture_url").isEqualTo(profileUrl)
                .jsonPath("$._links.self.href").exists()
                .jsonPath("$._links.tasks.href").exists()
                .jsonPath("$._links.projects.href").exists()
                .jsonPath("$._links.tags.href").exists();

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

    @AfterEach
    public void tearDown() {
        flyway.clean();
    }

    private String makeTokenFor(String email, String name, String profileUrl) {
        //a dummy token for test purposes
        String token = email + name + profileUrl;
        when(openIdTokenParser.extract(eq(token))).thenReturn(new OpenIdUser(email, name, profileUrl));
        return token;
    }

    private void authenticateWithToken(String accessToken) {
        client = client.mutateWith((builder, httpHandlerBuilder, connector) -> {
            builder.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
        });
    }

}
