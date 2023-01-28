package degallant.github.io.todoapp.validation;

import java.util.regex.Pattern;

public class ValidationRules {

    public static ValidationRule isPositive() {
        return new ValidationRule() {
            @Override
            public boolean isValid(String value) {
                try {
                    int number = Integer.parseInt(value);
                    return number > 0;
                } catch (NumberFormatException exception) {
                    return false;
                }
            }

            @Override
            public String messageId() {
                return "validation.positive.message";
            }
        };
    }

    public static ValidationRule isNotEmpty() {
        return new ValidationRule() {
            @Override
            public boolean isValid(String value) {
                return !value.isEmpty();
            }

            @Override
            public String messageId() {
                return "validation.notempty.message";
            }
        };
    }

    public static ValidationRule isDate() {
        return new ValidationRule() {
            @Override
            public boolean isValid(String value) {
                var pattern = Pattern.compile("^[12]\\d{3}-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01])$");
                var matcher = pattern.matcher(value);
                return matcher.matches();
            }

            @Override
            public String messageId() {
                return "validation.date.message";
            }
        };
    }

    public static ValidationRule isBoolean() {
        return new ValidationRule() {
            @Override
            public boolean isValid(String value) {
                return "true".equalsIgnoreCase(value) || "false".equals(value);
            }

            @Override
            public String messageId() {
                return "validation.boolean.message";
            }
        };
    }

}
