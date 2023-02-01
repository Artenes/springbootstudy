package degallant.github.io.todoapp.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.util.Strings;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/** @noinspection ClassCanBeRecord*/
public class Request {

    private final ClientProxy client;
    private final ObjectMapper mapper;
    private final Authenticator authenticator;

    public Request(ClientProxy client, Authenticator authenticator, ObjectMapper mapper) {
        this.client = client;
        this.authenticator = authenticator;
        this.mapper = mapper;
    }

    public Authenticated asUser(String email) {
        return new Authenticated(mapper, authenticator, email, client);
    }

    public Authenticated asGuest() {
        return new Authenticated(mapper, authenticator, null, client);
    }

    public static class Authenticated {

        private final ObjectMapper mapper;
        private final Authenticator authenticator;
        private final String email;
        private final ClientProxy client;

        private String path;
        private URI uri;

        public Authenticated(ObjectMapper mapper, Authenticator authenticator, String email, ClientProxy client) {
            this.mapper = mapper;
            this.authenticator = authenticator;
            this.email = email;
            this.client = client;
        }

        public Destination to(Object url) {

            if (url instanceof String) {
                if (((String) url).startsWith("http://")) {
                    this.uri = URI.create((String) url);
                } else {
                    this.path = (String) url;
                }
            }

            if (url instanceof URI) {
                this.uri = (URI) url;
            }

            return new Destination(email, client, mapper, authenticator, path, uri);
        }

    }

    public static class Destination {

        private final String email;
        private final ClientProxy client;
        private final ObjectMapper mapper;
        private final Authenticator authenticator;
        private final Map<String, Object> body = new HashMap<>();
        private final Map<String, Object> param = new HashMap<>();
        private final URI uri;
        private String path;

        public Destination(String email, ClientProxy client, ObjectMapper mapper, Authenticator authenticator, String path, URI uri) {
            this.email = email;
            this.client = client;
            this.mapper = mapper;
            this.authenticator = authenticator;
            this.path = path;
            this.uri = uri;
        }

        public Destination withField(String key, Object value) {
            body.put(key, value);
            return this;
        }

        public Destination withArray(String key, Object... value) {
            try {
                var array = mapper.writeValueAsString(value);
                body.put(key, array);
                return this;
            } catch (JsonProcessingException exception) {
                throw new RuntimeException(exception);
            }
        }

        public Destination withParam(String key, Object value) {
            param.put(key, value);
            return this;
        }

        public ExecutedRequest get() {
            authenticate();
            if (!param.isEmpty()) {
                var params = parseParams();
                path += "?" + params;
            }
            return new ExecutedRequest(mapper, resolveUri(client.get()).exchange());
        }

        public ExecutedRequest post() {
            authenticate();
            return new ExecutedRequest(mapper, resolveUri(client.post()).bodyValue(body).exchange());
        }

        public ExecutedRequest patch() {
            authenticate();
            return new ExecutedRequest(mapper, resolveUri(client.patch()).bodyValue(body).exchange());
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

        private WebTestClient.RequestHeadersSpec<?> resolveUri(WebTestClient.RequestHeadersUriSpec<?> spec) {
            if (uri != null) {
                return spec.uri(uri);
            } else {
                return spec.uri("/v1/" + path);
            }
        }

        private String parseParams() {
            var list = new ArrayList<String>();
            for (String key : param.keySet()) {
                list.add(key + "=" + param.get(key));
            }
            return Strings.join(list, '&');
        }

    }

}
