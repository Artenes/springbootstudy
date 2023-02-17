package degallant.github.io.todoapp.sanitization.parsers;

import degallant.github.io.todoapp.domain.tags.TagEntity;
import degallant.github.io.todoapp.domain.tags.TagsRepository;
import degallant.github.io.todoapp.domain.users.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.NoSuchElementException;
import java.util.UUID;

/**
 * @noinspection ClassCanBeRecord
 */
@Component
@RequiredArgsConstructor
public class TagsFieldParser {

    private final TagsRepository repository;

    public TagEntity toTagOrThrowNoSuchElement(String id, UserEntity user) throws NoSuchElementException {
        try {
            var uuid = UUID.fromString(id);
            return repository.findByIdAndUserIdAndDeletedAtIsNull(uuid, user.getId()).orElseThrow();
        } catch (IllegalArgumentException | NoSuchElementException exception) {
            throw new NoSuchElementException("No project with id " + id);
        }
    }

}
