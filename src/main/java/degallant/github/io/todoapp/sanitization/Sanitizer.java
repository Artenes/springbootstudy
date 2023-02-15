package degallant.github.io.todoapp.sanitization;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class Sanitizer {

    public SanitizedValue sanitizeSingle(ValueSpec spec) {
        try {
            return spec.sanitize();
        } catch (InvalidValueException exception) {
            throw new InvalidRequestException(makeError(spec, exception));
        }
    }

    public Map<String, SanitizedValue> sanitize(ValueSpec... specs) throws InvalidRequestException {

        var sanitizedFields = new HashMap<String, SanitizedValue>();
        var errors = new ArrayList<FieldAndErrorMessage>();

        for (ValueSpec spec : specs) {
            try {
                var field = spec.sanitize();
                sanitizedFields.put(spec.getName(), field);
            } catch (InvalidValueException exception) {
                errors.add(makeError(spec, exception));
            }
        }

        if (!errors.isEmpty()) {
            throw new InvalidRequestException(errors);
        }

        return sanitizedFields;
    }

    public ValueSpec field(String name) {
        return new ValueSpec("body", name);
    }

    public ValueSpec header(String name) {
        return new ValueSpec("header", name);
    }

    public ValueSpec param(String name) {
        return new ValueSpec("url", name);
    }

    private FieldAndErrorMessage makeError(ValueSpec spec, InvalidValueException exception) {
        return new FieldAndErrorMessage(spec.name, spec.getOrigin(), exception.getMessageId(), exception.getMessageArgs());
    }

    public static class ValueSpec {

        private final String origin;
        private final String name;
        private boolean required;
        private String value;
        private SanitizeSpec rule;
        private String sanitizedName;

        public ValueSpec(String origin, String name) {
            this.origin = origin;
            this.name = name;
        }

        public ValueSpec withRequiredValue(String value) {
            required = true;
            this.value = value;
            return this;
        }

        public ValueSpec withOptionalValue(String value) {
            required = false;
            this.value = value;
            return this;
        }

        public ValueSpec sanitize(SanitizeSpec rule) {
            this.rule = rule;
            return this;
        }

        public ValueSpec withName(String sanitizedName) {
            this.sanitizedName = sanitizedName;
            return this;
        }

        public String getName() {
            if (sanitizedName != null) {
                return sanitizedName;
            }
            return name;
        }

        public String getOrigin() {
            return origin;
        }

        public SanitizedValue sanitize() throws InvalidValueException {
            if (required && value == null) {
                throw new InvalidValueException("validation.is_required", name);
            }
            if (!required && value == null) {
                return new SanitizedValue(null);
            }
            return new SanitizedValue(rule.sanitize(value));
        }

        @Override
        public String toString() {
            return "FieldSpec{" +
                    "name='" + name + '\'' +
                    ", required=" + required +
                    ", value='" + value + '\'' +
                    '}';
        }

    }

}
