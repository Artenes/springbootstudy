package degallant.github.io.todoapp.validation;

public interface ValidationRule {

    boolean isValid(String value);

    String messageId();

}
