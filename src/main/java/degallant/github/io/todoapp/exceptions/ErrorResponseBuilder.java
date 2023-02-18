package degallant.github.io.todoapp.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.ErrorResponse;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class ErrorResponseBuilder {

    private final Exception exception;
    private HttpStatus status = HttpStatus.UNPROCESSABLE_ENTITY;
    private String detail = "";
    private URI uriType;
    private String title;
    private boolean debug;
    private final Map<String, Object> properties = new HashMap<>();

    public static ErrorResponseBuilder from(Exception exception) {
        return new ErrorResponseBuilder(exception);
    }

    private ErrorResponseBuilder(Exception exception) {
        this.exception = exception;
    }

    public ErrorResponseBuilder withStatus(HttpStatus status) {
        this.status = status;
        return this;
    }

    public ErrorResponseBuilder withDetail(String detail) {
        this.detail = detail;
        return this;
    }

    public ErrorResponseBuilder withType(URI type) {
        this.uriType = type;
        return this;
    }

    public ErrorResponseBuilder withTitle(String title) {
        this.title = title;
        return this;
    }

    public ErrorResponseBuilder withProperty(String name, Object value) {
        properties.put(name, value);
        return this;
    }

    public ErrorResponseBuilder withDebug(boolean debug) {
        this.debug = debug;
        return this;
    }

    public ErrorResponse build() {

        ErrorResponse.Builder builder = ErrorResponse.builder(
                exception,
                status,
                detail
        );

        if (uriType != null) {
            builder.type(uriType);
        }

        if (title != null && !title.isBlank()) {
            builder.title(title);
        }

        for (String key : properties.keySet()) {
            builder.property(key, properties.get(key));
        }

        if (debug) {
            builder.property("exception", new ExceptionDetails(exception));
        }

        return builder.build();

    }

}
