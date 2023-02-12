package degallant.github.io.todoapp.tasks;

import degallant.github.io.todoapp.common.LinkBuilder;
import degallant.github.io.todoapp.tags.TagsRepository;
import degallant.github.io.todoapp.users.UserEntity;
import degallant.github.io.todoapp.validation.FieldParser;
import degallant.github.io.todoapp.validation.FieldValidator;
import degallant.github.io.todoapp.validation.SanitizedField;
import degallant.github.io.todoapp.validation.Sanitizer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.Map;
import java.util.UUID;

/**
 * @noinspection ClassCanBeRecord
 */
@Service
@RequiredArgsConstructor
public class CreateTasksService {

    private final TasksRepository tasksRepository;
    private final TagsRepository tagsRepository;
    private final Sanitizer sanitizer;
    private final FieldValidator rules;
    private final FieldParser parser;
    private final LinkBuilder link;

    public URI create(TasksDto.Create request, UserEntity user) {

        var result = sanitizeRequest(request, user);

        var entity = TaskEntity.builder()
                .title(result.get("title").value())
                .description(result.get("description").value())
                .dueDate(result.get("due_date").value())
                .priority(result.get("priority").value())
                .tags(result.get("tags_ids").value())
                .parentId(result.get("parent_id").value())
                .user(user)
                .projectId(result.get("project_id").value())
                .complete(result.get("complete").asBool())
                .build();

        entity = tasksRepository.save(entity);

        return link.to("tasks").slash(entity.getId()).withSelfRel().toUri();
    }

    private Map<String, SanitizedField> sanitizeRequest(TasksDto.Create request, UserEntity user) {
        return sanitizer.sanitize(

                sanitizer.field("title").withRequiredValue(request.getTitle()).sanitize(value -> {
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

                sanitizer.field("parent_id").withOptionalValue(request.getParentId()).sanitize(value -> {
                    var parsed = parser.toUUID(value);
                    rules.taskBelongsToUser(parsed, user);
                    return parsed;
                }),

                sanitizer.field("project_id").withOptionalValue(request.getProjectId()).sanitize(value -> {
                    var parsed = parser.toUUID(value);
                    rules.projectBelongsToUser(parsed, user.getId());
                    return parsed;
                }),

                sanitizer.field("complete").withOptionalValue(request.getComplete()).sanitize(parser::toBoolean)

        );
    }

}
