package degallant.github.io.todoapp.validation;

import lombok.Getter;
import lombok.ToString;

/**
 * @noinspection ClassCanBeRecord
 */
@Getter
@ToString
public class ValidationStatus {

    private final boolean isValid;
    private final String messageId;
    private final Object[] messageArgs;

    public ValidationStatus(boolean isValid, String messageId, Object... messageArgs) {
        this.isValid = isValid;
        this.messageId = messageId;
        this.messageArgs = messageArgs;
    }

    public static ValidationStatus withStatus(boolean valid, String messageId, Object... args) {
        return new ValidationStatus(valid, messageId, args);
    }

    public static ValidationStatus withError(String messageId, Object... args) {
        return new ValidationStatus(false, messageId, args);
    }

    public static ValidationStatus withValidStatus() {
        return new ValidationStatus(true, null);
    }

}
