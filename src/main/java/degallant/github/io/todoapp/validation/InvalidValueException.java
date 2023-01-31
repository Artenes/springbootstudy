package degallant.github.io.todoapp.validation;

import lombok.Getter;

@Getter
public class InvalidValueException extends Exception {

    private final String messageId;
    private final Object[] messageArgs;

    public InvalidValueException(String messageId, Object... messageArgs) {
        this.messageId = messageId;
        this.messageArgs = messageArgs;
    }
}
