package degallant.github.io.todoapp.sanitization.parsers;

import degallant.github.io.todoapp.domain.users.UserEntity;
import degallant.github.io.todoapp.domain.users.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.NoSuchElementException;
import java.util.UUID;

/**
 * @noinspection ClassCanBeRecord
 */
@Component
@RequiredArgsConstructor
public class UsersFieldParser {

    private final UsersRepository repository;

    public UserEntity toUserOrThrowNoSuchElement(String id) {
        try {
            return repository.findByIdAndDeletedAtIsNull(UUID.fromString(id)).orElseThrow();
        } catch (IllegalArgumentException | NoSuchElementException exception) {
            throw new NoSuchElementException("No user found with id " + id, exception);
        }
    }

    public UserEntity toAbsoluteUserOrThrowNoSuchElement(String id) {
        try {
            return repository.findById(UUID.fromString(id)).orElseThrow();
        } catch (IllegalArgumentException | NoSuchElementException exception) {
            throw new NoSuchElementException("No user found with id " + id, exception);
        }
    }

}
