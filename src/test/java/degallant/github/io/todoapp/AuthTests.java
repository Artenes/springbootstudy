package degallant.github.io.todoapp;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.reactive.server.EntityExchangeResult;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

/** @noinspection unchecked, ConstantConditions */
public class AuthTests extends IntegrationTest {

    @Test
    public void failsAuthenticationWithoutToken() {
        client.post().uri("/v1/auth")
                .bodyValue(Map.of()).exchange()
                .expectStatus().isBadRequest();
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

}
