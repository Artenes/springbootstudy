package degallant.github.io.todoapp.domain.tasks;

import degallant.github.io.todoapp.sanitization.parsers.PrimitiveFieldParser;
import degallant.github.io.todoapp.sanitization.parsers.ProjectsFieldParser;
import degallant.github.io.todoapp.sanitization.parsers.TasksFieldParser;
import degallant.github.io.todoapp.domain.tags.TagsRepository;
import degallant.github.io.todoapp.domain.users.UserEntity;
import degallant.github.io.todoapp.sanitization.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * @noinspection ClassCanBeRecord
 */
@Service
@RequiredArgsConstructor
public class PatchTasksService {

    private final TasksRepository tasksRepository;
    private final TagsRepository tagsRepository;
    private final Sanitizer sanitizer;
    private final FieldValidator rules;
    private final PrimitiveFieldParser parser;
    private final TasksFieldParser tasksParser;
    private final ProjectsFieldParser projectParser;

    public boolean patch(String id, TasksDto.Create request, UserEntity user) {

        var entity = tasksParser.toTaskOrThrowNoSuchElement(id, user);
        var result = sanitizeRequest(request, user);

        if (!result.hasAnyFieldWithValue()) {
            return false;
        }

        result.get("title").consumeIfExists(entity::setTitle);
        result.get("description").consumeIfExists(entity::setDescription);
        result.get("due_date").consumeIfExists(entity::setDueDate);
        result.get("priority").consumeIfExists(entity::setPriority);
        result.get("complete").consumeIfExists(entity::setComplete);
        result.get("parent").consumeIfExists(entity::setParent);
        result.get("project").consumeIfExists(entity::setProject);
        result.get("tags").consumeIfExists(entity::setTags);

        tasksRepository.save(entity);

        return true;
    }

    private SanitizedCollection sanitizeRequest(TasksDto.Create request, UserEntity user) {
        return sanitizer.sanitize(

                sanitizer.field("title").withOptionalValue(request.getTitle()).sanitize(value -> {
                    rules.isNotEmpty(value);
                    return value;
                }),

                sanitizer.field("description").withOptionalValue(request.getDescription()).sanitize(value -> {
                    rules.isNotEmpty(value);
                    return value;
                }),

                sanitizer.field("due_date").withOptionalValue(request.getDueDate()).sanitize(value -> {
                    var parsed = parser.toOffsetDateTime(value);
                    rules.isPresentOrFuture(parsed);
                    return parsed;
                }),

                sanitizer.field("priority").withOptionalValue(request.getPriority()).sanitize(parser::toPriority),

                sanitizer.field("tags_ids").withOptionalValue(request.getTagsIds()).sanitize(value -> {
                    var parsed = parser.toUUIDList(value);
                    var found = tagsRepository.findAllByUserIdAndId(user.getId(), parsed);
                    rules.hasUnknownTag(parsed, found);
                    return found;
                }).withName("tags"),

                sanitizer.field("parent_id").withOptionalValue(request.getParentId())
                        .sanitize(value -> tasksParser.toTaskOrThrowInvalidValue(value, user)).withName("parent"),

                sanitizer.field("project_id").withOptionalValue(request.getProjectId())
                        .sanitize(value -> projectParser.toProjectOrThrowInvalidValue(value, user)).withName("project"),

                sanitizer.field("complete").withOptionalValue(request.getComplete()).sanitize(parser::toBoolean)

        );
    }

}
