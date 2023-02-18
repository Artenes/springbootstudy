package degallant.github.io.todoapp.sanitization.parsers;

import degallant.github.io.todoapp.authentication.ApiKeyEntity;
import degallant.github.io.todoapp.authentication.ApiKeyRepository;
import degallant.github.io.todoapp.sanitization.InvalidValueException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.NoSuchElementException;
import java.util.UUID;

/**
 * @noinspection ClassCanBeRecord
 */
@Component
@RequiredArgsConstructor
public class ApiKeyFieldParser {

    private final ApiKeyRepository repository;

    public ApiKeyEntity toApiKeyOrThrowInvalidValue(String id) throws InvalidValueException {
        try {
            return repository.findByIdAndDeletedAtIsNull(UUID.fromString(id)).orElseThrow();
        } catch (IllegalArgumentException | NoSuchElementException exception) {
            throw new InvalidValueException("validation.do_not_exist", id);
        }
    }

}
