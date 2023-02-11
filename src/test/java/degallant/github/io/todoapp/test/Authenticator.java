package degallant.github.io.todoapp.test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import degallant.github.io.todoapp.openid.OpenIdExtractionException;
import degallant.github.io.todoapp.openid.OpenIdTokenParser;
import degallant.github.io.todoapp.openid.OpenIdUser;
import net.minidev.json.JSONArray;
import org.junit.jupiter.api.Assertions;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.reactive.server.EntityExchangeResult;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * @noinspection unchecked, ClassCanBeRecord
 */
public class Authenticator {

    private final ClientProxy client;
    private final OpenIdTokenParser openIdTokenParser;
    private final ObjectMapper mapper;

    public Authenticator(ClientProxy client, OpenIdTokenParser openIdTokenParser, ObjectMapper mapper) {
        this.client = client;
        this.openIdTokenParser = openIdTokenParser;
        this.mapper = mapper;
    }

    public UUID makeUser(String email) {
        authenticate(email);
        EntityExchangeResult<byte[]> result = client.get().uri("/v1/auth/profile").exchange().expectBody().returnResult();
        try {
            var response = mapper.readValue(result.getResponseBodyContent(), JsonNode.class);
            return UUID.fromString(response.get("id").asText());
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    public void authenticate(String email) {
        String name = "Jhon Doe";
        String profileUrl = "https://google.com/profile/903jfiwfiwoe";
        String token = makeOpenIdTokenFor(email, name, profileUrl);
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

    public String makeOpenIdTokenFor(String email, String name, String profileUrl) {
        //a dummy token for test purposes
        String token = email + name + profileUrl;
        when(openIdTokenParser.extract(eq(token))).thenReturn(new OpenIdUser(email, name, profileUrl));
        return token;
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
