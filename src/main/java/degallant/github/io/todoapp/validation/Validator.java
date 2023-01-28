package degallant.github.io.todoapp.validation;

import degallant.github.io.todoapp.internationalization.Messages;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/** @noinspection ClassCanBeRecord*/
@Component
@RequiredArgsConstructor
public class Validator {

    private final Messages messages;

    public void validate(Validation... validations) {
        List<Validation> invalids = new ArrayList<>();

        for (Validation validation : validations) {
            if (!validation.isRequired() && !validation.isPresent()) {
                continue;
            }

            if (!validation.isPresent() || !validation.field()) {
                invalids.add(validation);
            }
        }

        if (invalids.isEmpty()) {
            return;
        }

        List<FieldAndError> errors = invalids.stream().map(item -> {
            var message = messages.get(item.getRule().messageId());
            return new FieldAndError(item.getField(), message);
        }).collect(Collectors.toList());

        throw new InvalidRequestException(errors);
    }

}
