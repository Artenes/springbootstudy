package degallant.github.io.todoapp.validation;

public interface ValidationRule {

    ValidationStatus isValid(String value);

}
