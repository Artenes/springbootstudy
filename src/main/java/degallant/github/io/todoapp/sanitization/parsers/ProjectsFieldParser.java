package degallant.github.io.todoapp.sanitization.parsers;

import degallant.github.io.todoapp.domain.projects.ProjectEntity;
import degallant.github.io.todoapp.domain.projects.ProjectsRepository;
import degallant.github.io.todoapp.domain.users.UserEntity;
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
public class ProjectsFieldParser {

    private final ProjectsRepository repository;

    public ProjectEntity toProjectOrThrowNoSuchElement(String id, UserEntity user) throws NoSuchElementException {
        try {
            return parse(id, user);
        } catch (IllegalArgumentException | NoSuchElementException exception) {
            throw new NoSuchElementException("No project with id " + id);
        }
    }

    public ProjectEntity toProjectOrThrowInvalidValue(String id, UserEntity user) throws InvalidValueException {
        try {
            return parse(id, user);
        } catch (IllegalArgumentException | NoSuchElementException exception) {
            throw new InvalidValueException("validation.do_not_exist", id);
        }
    }

    private ProjectEntity parse(String id, UserEntity user) {
        var projectId = UUID.fromString(id);
        return repository.findByIdAndUserIdAndDeletedAtIsNull(projectId, user.getId()).orElseThrow();
    }

}
