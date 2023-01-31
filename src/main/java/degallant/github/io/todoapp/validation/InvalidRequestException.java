package degallant.github.io.todoapp.validation;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;

@Getter
public class InvalidRequestException extends RuntimeException {

    private final List<FieldAndErrorMessage> errors;

    public InvalidRequestException(List<FieldAndErrorMessage> errors) {
        super(Arrays.toString(errors.toArray()));
        this.errors = errors;
    }
}
