package degallant.github.io.todoapp.validation;

import degallant.github.io.todoapp.tasks.TaskEntity;
import degallant.github.io.todoapp.tasks.TasksRepository;
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
public class TasksFieldParser {

    private final TasksRepository repository;

    public TaskEntity toTask(String id, UserEntity user) throws InvalidValueException {
        try {
            var taskId = UUID.fromString(id);
            var example = TaskEntity.belongsTo(taskId, user);
            return repository.findOne(example).orElseThrow();
        } catch (IllegalArgumentException | NoSuchElementException exception) {
            throw new InvalidValueException("validation.do_not_exist", id);
        }
    }

}
