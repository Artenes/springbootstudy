package degallant.github.io.todoapp.validation;

import degallant.github.io.todoapp.internationalization.Messages;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @noinspection ClassCanBeRecord
 */
@Component
@RequiredArgsConstructor
public class Validator {

    private final Messages messages;

    public void validate(Validation... validations) {
        List<FieldAndError> errors = new ArrayList<>();
        for (Validation validation : validations) {
            if (!validation.isRequired() && !validation.isPresent()) {
                continue;
            }

            if (!validation.isPresent()) {
                errors.add(new FieldAndError(validation.getField(), messages.get("validation.is_required", validation.getField())));
                continue;
            }

            var status = validation.isValid();

            if (!status.isValid()) {
                errors.add(new FieldAndError(validation.getField(), status.getErrorMessage()));
            }
        }

        if (errors.isEmpty()) {
            return;
        }
        throw new InvalidRequestException(errors);
    }

}
