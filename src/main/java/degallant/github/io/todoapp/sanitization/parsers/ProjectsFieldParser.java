package degallant.github.io.todoapp.sanitization.parsers;

import degallant.github.io.todoapp.projects.ProjectEntity;
import degallant.github.io.todoapp.projects.ProjectsRepository;
import degallant.github.io.todoapp.sanitization.InvalidValueException;
import degallant.github.io.todoapp.users.UserEntity;
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

    public ProjectEntity toProject(String id, UserEntity user) throws InvalidValueException {
        try {
            var projectId = UUID.fromString(id);
            return repository.findByIdAndUserId(projectId, user.getId()).orElseThrow();
        } catch (IllegalArgumentException | NoSuchElementException exception) {
            throw new InvalidValueException("validation.do_not_exist", id);
        }
    }

}
