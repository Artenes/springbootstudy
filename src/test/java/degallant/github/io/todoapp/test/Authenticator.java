package degallant.github.io.todoapp.test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import degallant.github.io.todoapp.openid.OpenIdExtractionException;
import degallant.github.io.todoapp.openid.OpenIdTokenParser;
import degallant.github.io.todoapp.openid.OpenIdUser;
import org.junit.jupiter.api.Assertions;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.EntityExchangeResult;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * @noinspection unchecked, ClassCanBeRecord, ConstantConditions
 */
public class Authenticator {

    private final ClientProxy client;
    private final OpenIdTokenParser openIdTokenParser;
    private final ObjectMapper mapper;
    private final UUID apiKey;

    public Authenticator(ClientProxy client, OpenIdTokenParser openIdTokenParser, ObjectMapper mapper, UUID apiKey) {
        this.client = client;
        this.openIdTokenParser = openIdTokenParser;
        this.mapper = mapper;
        this.apiKey = apiKey;
    }

    public UUID makeUser(String email) {
        authenticate(email);
        EntityExchangeResult<byte[]> result = client.get().uri("/v1/auth/profile").header("Client-Agent", apiKey.toString()).exchange().expectBody().returnResult();
        try {
            var response = mapper.readValue(result.getResponseBodyContent(), JsonNode.class);
            return UUID.fromString(response.get("id").asText());
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        } finally {
            clearAuthentication();
        }
    }

    public void authenticate(String email) {
        String name = "Jhon Doe";
        String profileUrl = "https://google.com/profile/903jfiwfiwoe";
        String token = makeOpenIdTokenFor(email, name, profileUrl);
        EntityExchangeResult<byte[]> result = client.post().uri("/v1/auth")
                .header("Client-Agent", apiKey.toString())
                .bodyValue(Map.of("open_id_token", token))
                .exchange()
                .expectBody().returnResult();
        try {
            if (result.getStatus() != HttpStatus.OK && result.getStatus() != HttpStatus.CREATED) {
                Assertions.fail("Authentication failed with status " + result.getStatus() + ". Response: " + new String(result.getResponseBodyContent()));
            }
            Map<String, String> response = mapper.readValue(result.getResponseBodyContent(), Map.class);
            authenticateWithToken(response.get("access_token"));
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    public void clearAuthentication() {
        client.mutateWith((builder, httpHandlerBuilder, connector)
                -> builder.defaultHeader(HttpHeaders.AUTHORIZATION));
    }

    public String makeOpenIdTokenFor(String email, String name, String profileUrl) {
        //a dummy token for test purposes
        String token = email + name + profileUrl;
        when(openIdTokenParser.extract(eq(token))).thenReturn(new OpenIdUser(email, name, profileUrl));
        return token;
    }

    public String makeOpenIdTokenFor(String email) {
        return makeOpenIdTokenFor(email, "Default name", "https://default.com/default.jpg");
    }

    public void makeParsingFailsTo(String token) {
        when(openIdTokenParser.extract(eq(token))).thenThrow(new OpenIdExtractionException.FailedParsing(token));
    }

    public void makeTokenInvalid(String token) {
        when(openIdTokenParser.extract(eq(token))).thenThrow(new OpenIdExtractionException.InvalidToken(token));
    }

    protected void authenticateWithToken(String accessToken) {
        client.mutateWith((builder, httpHandlerBuilder, connector)
                -> builder.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken));
    }

}
