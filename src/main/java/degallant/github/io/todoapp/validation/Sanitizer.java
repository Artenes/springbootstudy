package degallant.github.io.todoapp.validation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class Sanitizer {

    public Map<String, SanitizedField> sanitize(FieldSpec... specs) {

        var sanitizedFields = new HashMap<String, SanitizedField>();
        var errors = new ArrayList<FieldAndErrorMessage>();

        for (FieldSpec spec : specs) {
            try {
                var field = spec.sanitize();
                sanitizedFields.put(spec.name, field);
            } catch (InvalidValueException exception) {
                errors.add(makeError(spec, exception));
            }
        }

        if (!errors.isEmpty()) {
            throw new InvalidRequestException(errors);
        }

        return sanitizedFields;
    }

    public FieldSpec field(String name) {
        return new FieldSpec(name);
    }

    private FieldAndErrorMessage makeError(FieldSpec spec, InvalidValueException exception) {
        return new FieldAndErrorMessage(spec.name, exception.getMessageId(), exception.getMessageArgs());
    }

    public static class FieldSpec {

        private final String name;
        private boolean required;
        private String value;
        private SanitizeSpec rule;

        public FieldSpec(String name) {
            this.name = name;
        }

        public FieldSpec withRequiredValue(String value) {
            required = true;
            this.value = value;
            return this;
        }

        public FieldSpec withOptionalValue(String value) {
            required = false;
            this.value = value;
            return this;
        }

        public FieldSpec sanitize(SanitizeSpec rule) {
            this.rule = rule;
            return this;
        }

        public SanitizedField sanitize() throws InvalidValueException {
            if (required && value == null) {
                throw new InvalidValueException("validation.is_required", name);
            }
            if (!required && value == null) {
                return new SanitizedField(null);
            }
            return new SanitizedField(rule.sanitize(value));
        }

    }

}
