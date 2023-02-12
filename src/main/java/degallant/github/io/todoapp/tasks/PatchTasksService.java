package degallant.github.io.todoapp.tasks;

import degallant.github.io.todoapp.tags.TagsRepository;
import degallant.github.io.todoapp.users.UserEntity;
import degallant.github.io.todoapp.validation.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

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

    public void patch(String id, TasksDto.Create request, UserEntity user) {

        var entity = tasksParser.toTaskOrThrowNoSuchElement(id, user);
        var result = sanitizeRequest(request, user);

        entity.setTitle(result.get("title").ifNull(entity.getTitle()));
        entity.setDescription(result.get("description").ifNull(entity.getDescription()));
        entity.setDueDate(result.get("due_date").ifNull(entity.getDueDate()));
        entity.setPriority(result.get("priority").ifNull(entity.getPriority()));
        entity.setComplete(result.get("complete").ifNull(entity.getComplete()));
        entity.setParent(result.get("parent").ifNull(entity.getParent()));
        entity.setProject(result.get("project").ifNull(entity.getProject()));
        entity.setTags(result.get("tags_ids").ifNull(entity.getTags()));

        tasksRepository.save(entity);

    }

    private Map<String, SanitizedField> sanitizeRequest(TasksDto.Create request, UserEntity user) {
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
                }),

                sanitizer.field("parent_id").withOptionalValue(request.getParentId())
                        .sanitize(value -> tasksParser.toTaskOrThrowInvalidValue(value, user)).withName("parent"),

                sanitizer.field("project_id").withOptionalValue(request.getProjectId())
                        .sanitize(value -> projectParser.toProject(value, user)).withName("project"),

                sanitizer.field("complete").withOptionalValue(request.getComplete()).sanitize(parser::toBoolean)

        );
    }

}
