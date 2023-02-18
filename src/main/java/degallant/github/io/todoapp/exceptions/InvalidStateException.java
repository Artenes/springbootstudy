package degallant.github.io.todoapp.exceptions;

import lombok.Getter;

@Getter
public class InvalidStateException extends RuntimeException {

    private final String messageId;
    private final Object[] args;

    public InvalidStateException(String messageId, Object... args) {
        super("Invalid state, error: " + messageId);
        this.messageId = messageId;
        this.args = args;
    }

}
