package degallant.github.io.todoapp.authentication;

import lombok.Getter;

public class JwtTokenException extends RuntimeException {

    public JwtTokenException(Throwable cause) {
        super(cause);
    }

    @Getter
    public static class Expired extends JwtTokenException {

        public Expired(Throwable cause) {
            super(cause);
        }

    }

}
