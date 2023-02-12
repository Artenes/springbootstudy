package degallant.github.io.todoapp.validation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import degallant.github.io.todoapp.projects.ProjectEntity;
import degallant.github.io.todoapp.projects.ProjectsRepository;
import degallant.github.io.todoapp.tags.TagEntity;
import degallant.github.io.todoapp.tasks.TaskEntity;
import degallant.github.io.todoapp.tasks.TasksRepository;
import degallant.github.io.todoapp.users.UserEntity;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Component;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @noinspection ClassCanBeRecord, ResultOfMethodCallIgnored
 */
@Component
@RequiredArgsConstructor
public class FieldValidator {

    private final ObjectMapper mapper;
    private final TasksRepository tasksRepository;
    private final ProjectsRepository projectsRepository;

    public FieldValidator isPositive(int value) throws InvalidValueException {
        if (value < 0) {
            throwError("validation.is_positive", value);
        }
        return this;
    }

    public FieldValidator isNotEmpty(String value) throws InvalidValueException {
        if (value.isEmpty()) {
            throwError("validation.is_empty");
        }
        return this;
    }

    public FieldValidator isURL(String value) throws InvalidValueException {
        try {
            new URL(value).toURI();
        } catch (MalformedURLException | URISyntaxException exception) {
            throwError("validation.is_not_url", value);
        }
        return this;
    }

    public FieldValidator isDate(String value) throws InvalidValueException {
        var isValid = Pattern.compile("^[12]\\d{3}-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01])$").matcher(value).matches();
        if (!isValid) {
            throwError("validation.is_date", value);
        }
        return this;
    }

    public FieldValidator isBoolean(String value) throws InvalidValueException {
        var isValid = "true".equalsIgnoreCase(value) || "false".equals(value);
        if (!isValid) {
            throwError("validation.is_boolean", value);
        }
        return this;
    }

    public FieldValidator isPresentOrFuture(OffsetDateTime value) throws InvalidValueException {
        var now = OffsetDateTime.now();
        if (value.isBefore(now)) {
            throwError("validation.is_present_or_future", value);
        }
        return this;
    }

    public FieldValidator isPriority(String value) throws InvalidValueException {
        var isValid = Pattern.compile("^[Pp]1|[Pp]2|[Pp]3$").matcher(value).matches();
        if (!isValid) {
            throwError("validation.is_priority", value);
        }
        return this;
    }

    public FieldValidator areUuids(String value) throws InvalidValueException {
        try {
            String[] collection = mapper.readValue(value, String[].class);
            if (collection == null || collection.length == 0) {
                throwError("validation.empty_list");
            }
            var pattern = Pattern.compile("^[0-9a-fA-F]{8}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{12}$");
            for (int index = 0; index < collection.length; index++) {
                String uuid = collection[index];
                if (!pattern.matcher(uuid).matches()) {
                    throwError("validation.is_uuid_item", uuid, index);
                }
            }
        } catch (JsonProcessingException exception) {
            throwError("validation.not_a_valid_array", value);
        }
        return this;
    }

    public FieldValidator hasOnlyValidTags(UUID userId, List<UUID> tagIds) {
        return this;
    }

    public FieldValidator isUuid(String value) throws InvalidValueException {
        var isValid = Pattern.compile("^[0-9a-fA-F]{8}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{12}$").matcher(value).matches();
        if (!isValid) {
            throwError("validation.is_uuid", value);
        }
        return this;
    }

    public FieldValidator hasUnknownTag(List<UUID> tagIds, List<TagEntity> tags) throws InvalidValueException {
        var foundIds = tags.stream().map(TagEntity::getId).collect(Collectors.toList());
        var notFound = tagIds.stream().filter(id -> !foundIds.contains(id)).collect(Collectors.toList());
        if (!notFound.isEmpty()) {
            var ids = Strings.join(notFound, ',');
            throwError("validation.do_not_exist_list", ids);
        }
        return this;
    }

    public FieldValidator taskBelongsToUser(UUID taskId, UserEntity user) throws InvalidValueException {
        var example = TaskEntity.belongsTo(taskId, user);
        if (!tasksRepository.exists(example)) {
            throwError("validation.do_not_exist", taskId);
        }
        return this;
    }

    public InlineValidation check(boolean isValid) {
        return new InlineValidation(isValid);
    }

    public void throwError(String messageId, Object... args) throws InvalidValueException {
        throw new InvalidValueException(messageId, args);
    }

    @RequiredArgsConstructor
    public static class InlineValidation {

        private final boolean isValid;

        public void orThrow(String messageId, Object... args) throws InvalidValueException {
            if (!isValid) {
                throw new InvalidValueException(messageId, args);
            }
        }

    }

}
