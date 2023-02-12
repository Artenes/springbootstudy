package degallant.github.io.todoapp.tasks;

import degallant.github.io.todoapp.common.LinkBuilder;
import degallant.github.io.todoapp.tags.TagsRepository;
import degallant.github.io.todoapp.users.UserEntity;
import degallant.github.io.todoapp.validation.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.Map;

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
    private final PrimitiveFieldParser primitiveParser;
    private final TasksFieldParser tasksParser;
    private final ProjectsFieldParser projectsParser;
    private final LinkBuilder link;

    public URI create(TasksDto.Create request, UserEntity user) {

        var result = sanitizeRequest(request, user);

        var entity = TaskEntity.builder()
                .title(result.get("title").value())
                .description(result.get("description").value())
                .dueDate(result.get("due_date").value())
                .priority(result.get("priority").value())
                .tags(result.get("tags_ids").value())
                .parent(result.get("parent").value())
                .user(user)
                .project(result.get("project").value())
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
                    var parsed = primitiveParser.toOffsetDateTime(value);
                    rules.isPresentOrFuture(parsed);
                    return parsed;
                }),

                sanitizer.field("priority").withOptionalValue(request.getPriority()).sanitize(primitiveParser::toPriority),

                sanitizer.field("tags_ids").withOptionalValue(request.getTagsIds()).sanitize(value -> {
                    var parsed = primitiveParser.toUUIDList(value);
                    var found = tagsRepository.findAllByUserIdAndId(user.getId(), parsed);
                    rules.hasUnknownTag(parsed, found);
                    return found;
                }),

                sanitizer.field("parent_id").withOptionalValue(request.getParentId())
                        .sanitize(value -> tasksParser.toTaskOrThrowInvalidValue(value, user)).withName("parent"),

                sanitizer.field("project_id").withOptionalValue(request.getProjectId())
                        .sanitize(value -> projectsParser.toProject(value, user)).withName("project"),

                sanitizer.field("complete").withOptionalValue(request.getComplete()).sanitize(primitiveParser::toBoolean)

        );
    }

}
