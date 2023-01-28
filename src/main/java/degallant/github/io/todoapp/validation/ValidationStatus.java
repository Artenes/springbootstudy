package degallant.github.io.todoapp.validation;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** @noinspection ClassCanBeRecord*/
@Getter
@RequiredArgsConstructor
public class ValidationStatus {

    private final boolean isValid;
    private final String errorMessage;

    public static ValidationStatus withStatus(boolean valid, String messageInCaseOfError) {
        return new ValidationStatus(valid, messageInCaseOfError);
    }

    public static ValidationStatus withError(String message) {
        return new ValidationStatus(false, message);
    }

    public static ValidationStatus withValidStatus() {
        return new ValidationStatus(true, null);
    }

}
