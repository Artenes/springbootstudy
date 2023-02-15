package degallant.github.io.todoapp.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.util.Strings;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.reactive.server.JsonPathAssertions;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * @noinspection ClassCanBeRecord
 */
public class Request {

    private final ClientProxy client;
    private final Authenticator authenticator;
    private final ObjectMapper mapper;

    public Request(ClientProxy client, Authenticator authenticator, ObjectMapper mapper) {
        this.client = client;
        this.authenticator = authenticator;
        this.mapper = mapper;
    }

    public Authenticated asUser(String email) {
        var args = makeArgs();
        args.setAuthInfo(new AuthInfo(email, null));
        return new Authenticated(args);
    }

    public Authenticated asGuest() {
        var args = makeArgs();
        args.setAuthInfo(new AuthInfo());
        return new Authenticated(args);
    }

    public Authenticated withToken(String token) {
        var args = makeArgs();
        args.setAuthInfo(new AuthInfo(null, token));
        return new Authenticated(args);
    }

    private RequestArguments makeArgs() {
        return new RequestArguments(client, mapper, authenticator);
    }

    /**
     * @noinspection unused, ClassCanBeRecord
     */
    public static class Authenticated {

        private final RequestArguments arguments;

        public Authenticated(RequestArguments arguments) {
            this.arguments = arguments;
            this.arguments.setVersion(1);
        }

        public Authenticated usingVersion(int version) {
            arguments.setVersion(version);
            return this;
        }

        public Destination to(Object url) {

            if (url instanceof String) {
                if (((String) url).startsWith("http://")) {
                    arguments.setUri(URI.create((String) url));
                } else {
                    arguments.setPath((String) url);
                }
            }

            if (url instanceof URI) {
                arguments.setUri((URI) url);
            }

            return new Destination(arguments);
        }

    }

    /**
     * @noinspection ClassCanBeRecord
     */
    public static class Destination {

        private final RequestArguments arguments;

        public Destination(RequestArguments arguments) {
            this.arguments = arguments;
        }

        public Destination withBody(Object object) {
            arguments.setRawBody(object);
            return this;
        }

        public Destination withHeader(String header, Object value) {
            arguments.getHeaders().put(header, value);
            return this;
        }

        public Destination withField(String key, Object value) {
            arguments.getBody().put(key, value);
            return this;
        }

        public Destination withArray(String key, Object... value) {
            try {
                var array = arguments.getMapper().writeValueAsString(value);
                arguments.getBody().put(key, array);
                return this;
            } catch (JsonProcessingException exception) {
                throw new RuntimeException(exception);
            }
        }

        public Destination withParam(String key, Object value) {
            arguments.getParams().put(key, value);
            return this;
        }

        public ExecutedRequest get() {
            authenticate();
            setHeaders();
            if (!arguments.getParams().isEmpty()) {
                var params = parseParams();
                arguments.setPath(arguments.getPath() + "?" + params);
            }
            arguments.setResponseSpec(resolveUri(arguments.getClient().get()).exchange());
            return new ExecutedRequest(arguments);
        }

        public ExecutedRequest delete() {
            authenticate();
            setHeaders();
            arguments.setResponseSpec(resolveUri(arguments.getClient().delete()).exchange());
            return new ExecutedRequest(arguments);
        }

        public ExecutedRequest post() {
            authenticate();
            setHeaders();

            var spec = resolveUri(arguments.getClient().post());

            if (arguments.getRawBody() != null) {
                arguments.setResponseSpec(spec.bodyValue(arguments.getRawBody()).exchange());
                return new ExecutedRequest(arguments);
            }

            if (!arguments.getBody().isEmpty()) {
                arguments.setResponseSpec(spec.bodyValue(arguments.getBody()).exchange());
                return new ExecutedRequest(arguments);
            }

            arguments.setResponseSpec(spec.exchange());
            return new ExecutedRequest(arguments);
        }

        public ExecutedRequest patch() {
            authenticate();
            setHeaders();
            arguments.setResponseSpec(resolveUri(arguments.getClient().patch()).bodyValue(arguments.getBody()).exchange());
            return new ExecutedRequest(arguments);
        }

        private void authenticate() {
            if (arguments.getAuthInfo().email() != null) {
                arguments.getAuthenticator().authenticate(arguments.getAuthInfo().email());
                return;
            }

            if (arguments.getAuthInfo().token() != null) {
                arguments.getAuthenticator().authenticateWithToken(arguments.getAuthInfo().token());
                return;
            }

            arguments.getAuthenticator().clearAuthentication();
        }

        private WebTestClient.RequestBodySpec resolveUri(WebTestClient.RequestBodyUriSpec spec) {
            if (arguments.getUri() != null) {
                return spec.uri(arguments.getUri());
            } else {
                return spec.uri("/" + arguments.getVersion() + "/" + arguments.getPath());
            }
        }

        private WebTestClient.RequestHeadersSpec<?> resolveUri(WebTestClient.RequestHeadersUriSpec<?> spec) {
            if (arguments.getUri() != null) {
                return spec.uri(arguments.getUri());
            } else {
                return spec.uri("/" + arguments.getVersion() + "/" + arguments.getPath());
            }
        }

        private void setHeaders() {
            arguments.getClient().mutateWith((builder, httpHandlerBuilder, connector) -> {
                builder.defaultHeader(HttpHeaders.ACCEPT, "application/json");
                for (String header : arguments.getHeaders().keySet()) {
                    builder.defaultHeader(header, arguments.getHeaders().get(header).toString());
                }
            });
        }

        private String parseParams() {
            var list = new ArrayList<String>();
            for (String key : arguments.getParams().keySet()) {
                list.add(key + "=" + arguments.getParams().get(key));
            }
            return Strings.join(list, '&');
        }

    }

    /**
     * @noinspection ClassCanBeRecord, UnusedReturnValue
     */
    public static class ExecutedRequest {

        private final RequestArguments arguments;

        public ExecutedRequest(RequestArguments arguments) {
            this.arguments = arguments;
        }

        public ExecutedRequest isBadRequest() {
            arguments.setResponseSpec(arguments.getResponseSpec().expectStatus().isBadRequest());
            return this;
        }

        public ExecutedRequest isForbidden() {
            arguments.setResponseSpec(arguments.getResponseSpec().expectStatus().isForbidden());
            return this;
        }

        public ExecutedRequest isNotFound() {
            arguments.setResponseSpec(arguments.getResponseSpec().expectStatus().isNotFound());
            return this;
        }

        public ExecutedRequest isCreated() {
            arguments.setResponseSpec(arguments.getResponseSpec().expectStatus().isCreated());
            return this;
        }

        public ExecutedRequest isOk() {
            arguments.setResponseSpec(arguments.getResponseSpec().expectStatus().isOk());
            return this;
        }

        public ExecutedRequest isNoContent() {
            arguments.setResponseSpec(arguments.getResponseSpec().expectStatus().isNoContent());
            return this;
        }

        public URI getLocation() {
            return getBodySpec().returnResult().getResponseHeaders().getLocation();
        }

        public UUID getLocationUUID() {
            var uri = getLocation();
            var parts = uri.toString().split("/");
            return UUID.fromString(parts[parts.length - 1]);
        }

        public Response getResponse() {
            return new Response(getBody(), arguments.getResponse().getResponseHeaders());
        }

        public JsonNode getBody() {
            getBodySpec();
            try {
                return arguments.getMapper().readValue(arguments.getResponse().getResponseBodyContent(), JsonNode.class);
            } catch (IOException exception) {
                throw new RuntimeException(exception);
            }
        }

        public ExecutedRequest hasField(String path, Consumer<JsonPathAssertions> consumer) {
            try {
                consumer.accept(getBodySpec().jsonPath(path));
            } catch (AssertionError error) {
                show();
                throw error;
            }
            return this;
        }

        public ExecutedRequest show() {
            getBodySpec();
            if (arguments.getResponse() != null) {
                System.out.println(arguments.getResponse());
            }
            return this;
        }

        private WebTestClient.BodyContentSpec getBodySpec() {
            if (arguments.getBodySpec() == null) {
                arguments.setBodySpec(arguments.getResponseSpec().expectBody().consumeWith(arguments::setResponse));
            }
            return arguments.getBodySpec();
        }

        public record Response(JsonNode body, HttpHeaders headers) {

        }

    }

    public static record AuthInfo(String email, String token) {
        public AuthInfo() {
            this(null, null);
        }
    }

}
