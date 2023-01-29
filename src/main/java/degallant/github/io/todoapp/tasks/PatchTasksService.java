package degallant.github.io.todoapp.tasks;

import degallant.github.io.todoapp.internationalization.Messages;
import degallant.github.io.todoapp.projects.ProjectsRepository;
import degallant.github.io.todoapp.tags.TagsRepository;
import degallant.github.io.todoapp.users.UserEntity;
import degallant.github.io.todoapp.validation.PathValidator;
import degallant.github.io.todoapp.validation.ValidationRules;
import degallant.github.io.todoapp.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.util.Strings;
import org.springframework.data.domain.Example;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.stream.Collectors;

import static degallant.github.io.todoapp.validation.Validation.field;

/**
 * @noinspection ClassCanBeRecord
 */
@Service
@RequiredArgsConstructor
public class PatchTasksService {

    private final TasksRepository tasksRepository;
    private final ProjectsRepository projectsRepository;
    private final TagsRepository tagsRepository;
    private final Validator validator;
    private final ValidationRules rules;
    private final Messages messages;

    public void patch(String id, TasksDto.Create request, Authentication authentication) {

        UUID taskId = PathValidator.parseUUIDOrFail(id);
        UUID userId = ((UserEntity) authentication.getPrincipal()).getId();

        validator.validate(
                field("title", request.getTitle(), rules.isNotEmpty()),
                field("description", request.getDescription(), rules.isNotEmpty()),
                field("due_date", request.getDueDate(), rules.isPresentOrFuture()),
                field("priority", request.getPriority(), rules.isPriority()),
                field("tags_ids", request.getTagsIds(), rules.areUuids()),
                field("parent_id", request.getParentId(), rules.isUuid()),
                field("project_id", request.getProjectId(), rules.isUuid()),
                field("complete", request.getComplete(), rules.isBoolean())
        );

        var example = Example.of(
                TaskEntity.builder()
                        .id(taskId)
                        .userId(userId)
                        .build()
        );

        var entity = tasksRepository.findOne(example).orElseThrow();

        if (request.getTitle() != null) {
            entity.setTitle(request.getTitle());
        }

        if (request.getDescription() != null) {
            entity.setDescription(request.getDescription());
        }

        if (request.getDueDate() != null) {
            entity.setDueDate(request.getDueDateAsOffsetDateTime());
        }

        if (request.getPriority() != null) {
            entity.setPriority(request.getPriorityAsEnum());
        }

        if (request.getTagsIds() != null) {
            var tags = tagsRepository.findAllById(request.getTagsIdsAsUUID());
            var notFound = request.getTagsIdsAsUUID().stream()
                    .filter(tagId -> tags.stream().filter(tag -> tag.getId() == tagId).findFirst().isEmpty())
                    .collect(Collectors.toList());
            if (!notFound.isEmpty()) {
                validator.throwErrorForField("tags_ids", messages.get("validation.do_not_exist_list", Strings.join(notFound, ',')));
            }
            entity.setTags(tags);
        }

        if (request.getComplete() != null) {
            entity.setComplete(request.getCompleteAsBoolean());
        }

        if (request.getParentId() != null) {
            if (!tasksRepository.existsById(request.getParentIdAsUUID())) {
                validator.throwErrorForField("parent_id", messages.get("validation.do_not_exist", request.getProjectId()));
            }
            entity.setParentId(request.getParentIdAsUUID());
        }

        if (request.getProjectId() != null) {
            if (!projectsRepository.existsById(request.getProjectIdAsUUID())) {
                validator.throwErrorForField("project_id", messages.get("validation.do_not_exist", request.getProjectId()));
            }
            entity.setProjectId(request.getProjectIdAsUUID());
        }

        tasksRepository.save(entity);

    }

}
