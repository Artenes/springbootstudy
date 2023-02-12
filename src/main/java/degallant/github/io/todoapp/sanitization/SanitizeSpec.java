package degallant.github.io.todoapp.sanitization;

public interface SanitizeSpec {

    Object sanitize(String value) throws InvalidValueException;

}
