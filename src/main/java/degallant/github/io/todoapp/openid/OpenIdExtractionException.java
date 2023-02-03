package degallant.github.io.todoapp.openid;

import lombok.Getter;

@Getter
public class OpenIdExtractionException extends RuntimeException {

    private final String token;

    private OpenIdExtractionException(String token) {
        super();
        this.token = token;
    }

    private OpenIdExtractionException(String token, Throwable cause) {
        super(cause);
        this.token = token;
    }

    public static class FailedParsing extends OpenIdExtractionException {

        public FailedParsing(String token, Throwable cause) {
            super(token, cause);
        }

        public FailedParsing(String token) {
            super(token);
        }

    }

    public static class InvalidToken extends OpenIdExtractionException {

        public InvalidToken(String token) {
            super(token);
        }

    }

}
