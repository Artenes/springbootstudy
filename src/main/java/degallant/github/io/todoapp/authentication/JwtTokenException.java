package degallant.github.io.todoapp.authentication;

public class JwtTokenException extends RuntimeException {

    public JwtTokenException() {
    }

    public JwtTokenException(Throwable cause) {
        super(cause);
    }

    public static class Expired extends JwtTokenException {

        public Expired(Throwable cause) {
            super(cause);
        }

    }

    public static class Invalid extends JwtTokenException {
        public Invalid(Throwable cause) {
            super(cause);
        }
    }

    public static class InvalidSubject extends JwtTokenException {
        public InvalidSubject(Throwable cause) {
            super(cause);
        }

        public InvalidSubject() {
        }
    }

    public static class InvalidClaim extends JwtTokenException {
        public InvalidClaim(Throwable exception) {
            super(exception);
        }
    }
}
