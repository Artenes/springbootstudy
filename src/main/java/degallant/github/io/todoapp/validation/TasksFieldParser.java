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

    public TaskEntity toTaskOrThrowNoSuchElement(String id, UserEntity user) throws NoSuchElementException {
        try {
            return parse(id, user);
        } catch (IllegalArgumentException | NoSuchElementException exception) {
            throw new NoSuchElementException("Task with id " + id + " not found", exception);
        }
    }

    public TaskEntity toTaskOrThrowInvalidValue(String id, UserEntity user) throws InvalidValueException {
        try {
            return parse(id, user);
        } catch (IllegalArgumentException | NoSuchElementException exception) {
            throw new InvalidValueException("validation.do_not_exist", id);
        }
    }

    private TaskEntity parse(String id, UserEntity user) {
        var taskId = UUID.fromString(id);
        return repository.findByIdAndUserId(taskId, user.getId()).orElseThrow();
    }

}
