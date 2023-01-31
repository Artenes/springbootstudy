package degallant.github.io.todoapp.common;

import org.springframework.test.web.reactive.server.JsonPathAssertions;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.net.URI;
import java.util.UUID;
import java.util.function.Consumer;

public class ExecutedRequest {

    private WebTestClient.ResponseSpec responseSpec;
    private WebTestClient.BodyContentSpec bodySpec;

    public ExecutedRequest(WebTestClient.ResponseSpec responseSpec) {
        this.responseSpec = responseSpec;
    }

    public ExecutedRequest isBadRequest() {
        responseSpec = responseSpec.expectStatus().isBadRequest();
        return this;
    }

    public ExecutedRequest isCreated() {
        responseSpec = responseSpec.expectStatus().isCreated();
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

    public ExecutedRequest hasField(String path, Consumer<JsonPathAssertions> consumer) {
        consumer.accept(getBodySpec().jsonPath(path));
        return this;
    }

    public ExecutedRequest show() {
        if (bodySpec != null) {
            bodySpec.consumeWith(System.out::println);
            return this;
        }
        getBodySpec().consumeWith(System.out::println);
        return this;
    }

    private WebTestClient.BodyContentSpec getBodySpec() {
        if (bodySpec == null) {
            bodySpec = responseSpec.expectBody();
        }
        return bodySpec;
    }

}
