package degallant.github.io.todoapp.sanitization;

public record FieldAndErrorMessage(String field, String origin, String errorId, Object... errorArgs) {

}
