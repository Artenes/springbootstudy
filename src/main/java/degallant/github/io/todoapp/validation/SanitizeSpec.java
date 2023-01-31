package degallant.github.io.todoapp.validation;

public interface SanitizeSpec {

    Object sanitize(String value) throws InvalidValueException;

}
