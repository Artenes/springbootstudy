package degallant.github.io.todoapp.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class AuthenticatedRequest {

    private final ObjectMapper mapper;
    private final Authenticator authenticator;
    private final String email;
    private final ClientProxy client;

    private String path;
    private URI uri;
    private final Map<String, Object> body = new HashMap<>();
    private final Map<String, Object> param = new HashMap<>();

    public AuthenticatedRequest(ObjectMapper mapper, Authenticator authenticator, String email, ClientProxy client) {
        this.mapper = mapper;
        this.authenticator = authenticator;
        this.email = email;
        this.client = client;
    }

    public AuthenticatedRequest to(String path) {
        this.path = path;
        return this;
    }

    public AuthenticatedRequest to(URI uri) {
        this.uri = uri;
        return this;
    }

    public AuthenticatedRequest withField(String key, Object value) {
        body.put(key, value);
        return this;
    }

    public AuthenticatedRequest withArray(String key, Object... value) {
        try {
            var array = mapper.writeValueAsString(value);
            body.put(key, array);
            return this;
        } catch (JsonProcessingException exception) {
            throw new RuntimeException(exception);
        }
    }

    public AuthenticatedRequest withParam(String key, Object value) {
        param.put(key, value);
        return this;
    }

    public ExecutedRequest post() {
        authenticate();
        return new ExecutedRequest(resolveUri(client.post()).bodyValue(body).exchange());
    }

    private void authenticate() {
        if (email != null) {
            authenticator.authenticate(email);
        }
    }

    private WebTestClient.RequestBodySpec resolveUri(WebTestClient.RequestBodyUriSpec spec) {
        if (uri != null) {
            return spec.uri(uri);
        } else {
            return spec.uri("/v1/" + path);
        }
    }

}
