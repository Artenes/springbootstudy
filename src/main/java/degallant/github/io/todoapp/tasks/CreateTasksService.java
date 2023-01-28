package degallant.github.io.todoapp.tasks;

import degallant.github.io.todoapp.common.LinkBuilder;
import degallant.github.io.todoapp.tags.TagEntity;
import degallant.github.io.todoapp.tags.TagsRepository;
import degallant.github.io.todoapp.users.UserEntity;
import degallant.github.io.todoapp.validation.ValidationRules;
import degallant.github.io.todoapp.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.Collections;
import java.util.List;

import static degallant.github.io.todoapp.validation.Validation.field;

/**
 * @noinspection ClassCanBeRecord
 */
@Service
@RequiredArgsConstructor
public class CreateTasksService {

    private final TasksRepository tasksRepository;
    private final TagsRepository tagsRepository;
    private final Validator validator;
    private final ValidationRules rules;

    public URI create(TasksDto.Create request, Authentication authentication) {

        validator.validate(
                field("title", request.getTitle(), rules.isNotEmpty(), true),
                field("description", request.getDescription(), rules.isNotEmpty()),
                field("due_date", request.getDueDate(), rules.isPresentOrFuture()),
                field("priority", request.getPriority(), rules.isPriority()),
                field("tags_ids", request.getTagsIds(), rules.areUuids()),
                field("parent_id", request.getParentId(), rules.isUuid()),
                field("project_id", request.getProjectId(), rules.isUuid()),
                field("complete", request.getComplete(), rules.isBoolean())
        );

        //tags are created beforehand
        //so we just query its instances to then pass in the to do entity below
        List<TagEntity> tags = Collections.emptyList();
        if (request.getTagsIds() != null && !request.getTagsIds().isEmpty()) {
            tags = tagsRepository.findAllById(request.getTagsIdsAsUUID());
        }

        var userId = ((UserEntity) authentication.getPrincipal()).getId();

        var entity = TaskEntity.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .dueDate(request.getDueDateAsOffsetDateTime())
                .priority(request.getPriorityAsEnum())
                .tags(tags)
                .parentId(request.getParentIdAsUUID())
                .userId(userId)
                .projectId(request.getProjectIdAsUUID())
                .complete(request.getCompleteAsBoolean())
                .build();

        entity = tasksRepository.save(entity);

        return LinkBuilder.makeLinkTo("v1", "tasks", entity.getId()).withSelfRel().toUri();
    }

}
