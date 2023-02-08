package degallant.github.io.todoapp.authentication;

public class JwtTokenException extends RuntimeException {

    private final String token;

    public JwtTokenException(String token) {
        super("Invalid token " + token);
        this.token = token;
    }

    public JwtTokenException(Throwable cause, String token) {
        super("Invalid token " + token, cause);
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public static class Expired extends JwtTokenException {

        public Expired(Throwable cause, String token) {
            super(cause, token);
        }

    }

    public static class Invalid extends JwtTokenException {
        public Invalid(Throwable cause, String token) {
            super(cause, token);
        }
    }

    public static class InvalidSubject extends JwtTokenException {
        public InvalidSubject(String token) {
            super(token);
        }

        public InvalidSubject(Throwable cause, String token) {
            super(cause, token);
        }
    }

    public static class InvalidClaim extends JwtTokenException {
        public InvalidClaim(Throwable cause, String token) {
            super(cause, token);
        }
    }
}
