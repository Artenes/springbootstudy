package degallant.github.io.todoapp.validation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import degallant.github.io.todoapp.comments.CommentEntity;
import degallant.github.io.todoapp.comments.CommentsRepository;
import degallant.github.io.todoapp.common.SortParsingException;
import degallant.github.io.todoapp.common.SortingParser;
import degallant.github.io.todoapp.tasks.Priority;
import degallant.github.io.todoapp.tasks.TaskEntity;
import degallant.github.io.todoapp.tasks.TasksRepository;
import degallant.github.io.todoapp.users.Role;
import degallant.github.io.todoapp.users.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

/**
 * @noinspection ClassCanBeRecord
 */
@Component
@RequiredArgsConstructor
public class PrimitiveFieldParser {

    private final ObjectMapper mapper;
    private final TasksRepository tasksRepository;
    private final CommentsRepository commentsRepository;
    private final SortingParser sortingParser;

    public Sort toSort(String value, String... validAttributes) throws InvalidValueException {
        try {
            return sortingParser.parse(value, validAttributes);
        } catch (SortParsingException exception) {
            if (exception instanceof SortParsingException.InvalidAttribute) {
                var attribute = ((SortParsingException.InvalidAttribute) exception).getAttribute();
                throw new InvalidValueException("error.invalid_sort_attribute.detail", attribute);
            } else if (exception instanceof SortParsingException.InvalidDirection) {
                var direction = ((SortParsingException.InvalidDirection) exception).getDirection();
                throw new InvalidValueException("error.invalid_sort_direction.detail", direction);
            } else {
                var query = ((SortParsingException.InvalidQuery) exception).getQuery();
                throw new InvalidValueException("error.invalid_sort_query.detail", query);
            }
        }
    }

    public boolean toBoolean(String value) throws InvalidValueException {
        if ("true".equalsIgnoreCase(value)) {
            return true;
        }

        if ("false".equalsIgnoreCase(value)) {
            return false;
        }

        throw new InvalidValueException("validation.is_boolean", value);
    }

    public int toInteger(String value) throws InvalidValueException {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            throw new InvalidValueException("validation.is_not_a_number", value);
        }
    }

    public UUID toUUID(String value) throws InvalidValueException {
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException exception) {
            throw new InvalidValueException("validation.is_uuid", value);
        }
    }

    public UUID toUuidOrThrow(String value) throws NoSuchElementException {
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException exception) {
            throw new NoSuchElementException("Invalid UUID " + value);
        }
    }

    public LocalDate toLocalDate(String value) throws InvalidValueException {
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException exception) {
            throw new InvalidValueException("validation.is_date", value);
        }
    }

    public OffsetDateTime toOffsetDateTime(String value) throws InvalidValueException {
        try {
            return OffsetDateTime.parse(value);
        } catch (DateTimeParseException exception) {
            throw new InvalidValueException("validation.not_a_valid_date", value);
        }
    }

    public Priority toPriority(String value) throws InvalidValueException {
        var priority = Arrays.stream(Priority.values())
                .filter(p -> p.name().equalsIgnoreCase(value))
                .findFirst();
        if (priority.isEmpty()) {
            throw new InvalidValueException("validation.is_priority", value);
        }
        return priority.get();
    }

    public List<UUID> toUUIDList(String value) throws InvalidValueException {
        try {
            var array = mapper.readValue(value, UUID[].class);
            return Arrays.stream(array).toList();
        } catch (JsonProcessingException exception) {
            throw new InvalidValueException(exception, "validation.invalid_id_list", value);
        }
    }

    public Role toRole(String value) throws InvalidValueException {
        try {
            return Arrays.stream(Role.values()).filter(role -> role.name().equalsIgnoreCase(value)).findFirst().orElseThrow();
        } catch (NoSuchElementException exception) {
            throw new InvalidValueException(exception, "validation.is_not_role", value);
        }
    }

    public TaskEntity toTask(String id, UserEntity user) throws NoSuchElementException {
        try {
            var taskId = UUID.fromString(id);
            var example = TaskEntity.belongsTo(taskId, user);
            return tasksRepository.findOne(example).orElseThrow();
        } catch (IllegalArgumentException | NoSuchElementException exception) {
            throw new NoSuchElementException("No task found with id " + id, exception);
        }
    }

    public CommentEntity toComment(String id, UUID taskId, UUID userId) throws NoSuchElementException {
        try {
            var commentId = UUID.fromString(id);
            var example = CommentEntity.belongsTo(commentId, taskId, userId);
            return commentsRepository.findOne(example).orElseThrow();
        } catch (IllegalArgumentException | NoSuchElementException exception) {
            throw new NoSuchElementException("No comment found with id " + id, exception);
        }
    }
}