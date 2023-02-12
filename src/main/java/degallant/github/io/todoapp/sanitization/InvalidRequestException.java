package degallant.github.io.todoapp.sanitization;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;

@Getter
public class InvalidRequestException extends RuntimeException {

    private final List<FieldAndErrorMessage> errors;

    public InvalidRequestException(List<FieldAndErrorMessage> errors) {
        super(Arrays.toString(errors.stream()
                .map(item ->
                        String.format("field=%s, error=%s, args=%s", item.field(), item.errorId(), Arrays.toString(item.errorArgs()))
                ).toArray()));
        this.errors = errors;
    }
}
