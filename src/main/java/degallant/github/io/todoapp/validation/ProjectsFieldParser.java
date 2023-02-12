package degallant.github.io.todoapp.validation;

import degallant.github.io.todoapp.projects.ProjectEntity;
import degallant.github.io.todoapp.projects.ProjectsRepository;
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
            var taskId = UUID.fromString(id);
            var example = ProjectEntity.belongsTo(taskId, user);
            return repository.findOne(example).orElseThrow();
        } catch (IllegalArgumentException | NoSuchElementException exception) {
            throw new InvalidValueException("validation.do_not_exist", id);
        }
    }

}
