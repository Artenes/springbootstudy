package degallant.github.io.todoapp.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.util.Strings;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @noinspection ClassCanBeRecord
 */
public class Request {

    //TODO add option to change version
    private final ClientProxy client;
    private final ObjectMapper mapper;
    private final Authenticator authenticator;

    public Request(ClientProxy client, Authenticator authenticator, ObjectMapper mapper) {
        this.client = client;
        this.authenticator = authenticator;
        this.mapper = mapper;
    }

    public Authenticated asUser(String email) {
        return new Authenticated(mapper, authenticator, new AuthInfo(email, null), client);
    }

    public Authenticated asGuest() {
        return new Authenticated(mapper, authenticator, new AuthInfo(), client);
    }

    public Authenticated withToken(String token) {
        return new Authenticated(mapper, authenticator, new AuthInfo(null, token), client);
    }

    public static record AuthInfo(String email, String token) {
        public AuthInfo() {
            this(null, null);
        }
    }

    /** @noinspection unused*/
    public static class Authenticated {

        private final ObjectMapper mapper;
        private final Authenticator authenticator;
        private final AuthInfo authInfo;
        private final ClientProxy client;

        private String path;
        private URI uri;
        private String version;

        public Authenticated(ObjectMapper mapper, Authenticator authenticator, AuthInfo authInfo, ClientProxy client) {
            this.mapper = mapper;
            this.authenticator = authenticator;
            this.authInfo = authInfo;
            this.client = client;
            this.version = "v1";
        }

        public Authenticated usingVersion(int version) {
            this.version = "v"+version;
            return this;
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

            return new Destination(version, authInfo, client, mapper, authenticator, path, uri);
        }

    }

    public static class Destination {

        private final String version;
        private final AuthInfo authInfo;
        private final ClientProxy client;
        private final ObjectMapper mapper;
        private final Authenticator authenticator;
        private final Map<String, Object> body = new HashMap<>();
        private final Map<String, Object> param = new HashMap<>();
        private Object rawBody;
        private final URI uri;
        private String path;

        public Destination(String version, AuthInfo authInfo, ClientProxy client, ObjectMapper mapper, Authenticator authenticator, String path, URI uri) {
            this.version = version;
            this.authInfo = authInfo;
            this.client = client;
            this.mapper = mapper;
            this.authenticator = authenticator;
            this.path = path;
            this.uri = uri;
        }

        public Destination withBody(Object object) {
            this.rawBody = object;
            return this;
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
            var spec = resolveUri(client.post());

            if (rawBody != null) {
                return new ExecutedRequest(mapper, spec.bodyValue(rawBody).exchange());
            }

            if (!body.isEmpty()) {
                return new ExecutedRequest(mapper, spec.bodyValue(body).exchange());
            }

            return new ExecutedRequest(mapper, spec.exchange());
        }

        public ExecutedRequest patch() {
            authenticate();
            return new ExecutedRequest(mapper, resolveUri(client.patch()).bodyValue(body).exchange());
        }

        private void authenticate() {
            if (authInfo.email() != null) {
                authenticator.authenticate(authInfo.email());
                return;
            }

            if (authInfo.token() != null) {
                authenticator.authenticateWithToken(authInfo.token());
            }
        }

        private WebTestClient.RequestBodySpec resolveUri(WebTestClient.RequestBodyUriSpec spec) {
            if (uri != null) {
                return spec.uri(uri);
            } else {
                return spec.uri("/"+version+"/" + path);
            }
        }

        private WebTestClient.RequestHeadersSpec<?> resolveUri(WebTestClient.RequestHeadersUriSpec<?> spec) {
            if (uri != null) {
                return spec.uri(uri);
            } else {
                return spec.uri("/"+version+"/" + path);
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
