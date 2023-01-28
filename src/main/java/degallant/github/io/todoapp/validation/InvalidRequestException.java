package degallant.github.io.todoapp.validation;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
@Getter
public class InvalidRequestException extends RuntimeException {

    private final List<FieldAndError> errors;

}
