package degallant.github.io.todoapp.common;

import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.JsonPathAssertions;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.net.URI;
import java.util.UUID;
import java.util.function.Consumer;

public class ExecutedRequest {

    private WebTestClient.ResponseSpec responseSpec;
    private WebTestClient.BodyContentSpec bodySpec;
    private EntityExchangeResult<byte[]> response;

    public ExecutedRequest(WebTestClient.ResponseSpec responseSpec) {
        this.responseSpec = responseSpec;
    }

    public ExecutedRequest isBadRequest() {
        responseSpec = responseSpec.expectStatus().isBadRequest();
        return this;
    }

    public ExecutedRequest isNotFound() {
        responseSpec = responseSpec.expectStatus().isNotFound();
        return this;
    }

    public ExecutedRequest isCreated() {
        responseSpec = responseSpec.expectStatus().isCreated();
        return this;
    }

    public ExecutedRequest isOk() {
        responseSpec = responseSpec.expectStatus().isOk();
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
        if (response != null) {
            System.out.println(response);
        }
        return this;
    }

    private WebTestClient.BodyContentSpec getBodySpec() {
        if (bodySpec == null) {
            bodySpec = responseSpec.expectBody().consumeWith(value -> {
                response = value;
            });
        }
        return bodySpec;
    }

}
