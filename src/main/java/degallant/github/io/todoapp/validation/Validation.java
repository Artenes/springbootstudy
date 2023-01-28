package degallant.github.io.todoapp.validation;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** @noinspection BooleanMethodIsAlwaysInverted*/
@RequiredArgsConstructor
@Getter
public class Validation {

    private final String field;
    private final String value;
    private final ValidationRule rule;
    private final boolean required;

    public static Validation field(String field, String value, ValidationRule rule) {
        return new Validation(field, value, rule);
    }

    public static Validation field(String field, String value, ValidationRule rule, boolean isRequired) {
        return new Validation(field, value, rule, isRequired);
    }

    public Validation(String field, String value, ValidationRule rule) {
        this.field = field;
        this.value = value;
        this.rule = rule;
        this.required = false;
    }

    public boolean isPresent() {
        return value != null;
    }

    public ValidationStatus isValid() {
        return rule.isValid(value);
    }

}
