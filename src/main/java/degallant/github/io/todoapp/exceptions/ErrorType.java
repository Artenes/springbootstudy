package degallant.github.io.todoapp.exceptions;

import java.net.URI;

public enum ErrorType {

    INVALID_REQUEST("invalid-request"),
    INVALID_TOKEN("invalid-token"),
    UNPROCESSABLE_REQUEST("unprocessable-request"),
    NO_SUCH_ELEMENT("no-such-element"),
    INTERNAL_SERVER_ERROR("internal-server-error");

    private final URI uri;

    ErrorType(String path) {
        this.uri = URI.create("https://todoapp.com/" + path);
    }

    public URI getUri() {
        return uri;
    }

}
