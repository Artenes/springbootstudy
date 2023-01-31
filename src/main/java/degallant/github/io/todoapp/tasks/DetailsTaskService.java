package degallant.github.io.todoapp.tasks;

import degallant.github.io.todoapp.projects.ProjectsDto;
import degallant.github.io.todoapp.projects.ProjectsRepository;
import degallant.github.io.todoapp.tags.TagEntity;
import degallant.github.io.todoapp.tags.TagsDto;
import degallant.github.io.todoapp.users.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.mediatype.hal.HalModelBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

import static degallant.github.io.todoapp.common.LinkBuilder.makeLinkTo;
import static degallant.github.io.todoapp.validation.PathValidator.parseUUIDOrFail;

/**
 * @noinspection ClassCanBeRecord
 */
@Service
@RequiredArgsConstructor
public class DetailsTaskService {

    private final TasksRepository tasksRepository;
    private final ProjectsRepository projectsRepository;

    public RepresentationModel<?> details(String rawId, Authentication authentication) {

        var id = parseUUIDOrFail(rawId);
        var userId = ((UserEntity) authentication.getPrincipal()).getId();
        var entity = tasksRepository.findByIdAndUserId(id, userId).orElseThrow();

        var task = TasksDto.DetailsComplete.builder()
                .title(entity.getTitle())
                .description(entity.getDescription())
                .dueDate(entity.getDueDate())
                .priority(entity.getPriority())
                .complete(entity.getComplete());

        var response = HalModelBuilder.emptyHalModel();

        if (entity.getTags() != null && !entity.getTags().isEmpty()) {
            var tags = entity.getTags()
                    .stream()
                    .map(this::toEntityModel)
                    .collect(Collectors.toList());
            response.embed(tags);
        }

        var children = tasksRepository.findByParentId(entity.getId());
        if (children != null && !children.isEmpty()) {
            var subTasks = children
                    .stream()
                    .map(this::toEntityModel)
                    .collect(Collectors.toList());
            response.embed(subTasks);
        }

        if (entity.getParentId() != null) {
            var parentEntity = tasksRepository.findByIdAndUserId(entity.getParentId(), userId).orElseThrow();
            var parent = TasksDto.ParentTask.builder()
                    .id(parentEntity.getId())
                    .title(parentEntity.getTitle())
                    .build();
            var linkSelf = makeLinkTo("v1", "tasks", parentEntity.getId()).withSelfRel();
            response.embed(EntityModel.of(parent).add(linkSelf));
        }

        if (entity.getProjectId() != null) {
            var projectEntity = projectsRepository.findByIdAndUserId(entity.getProjectId(), userId).orElseThrow();
            var project = ProjectsDto.Details.builder()
                    .id(projectEntity.getId())
                    .title(projectEntity.getTitle())
                    .build();
            var linkSelf = makeLinkTo("v1", "projects", projectEntity.getId()).withSelfRel();
            response.embed(EntityModel.of(project).add(linkSelf));
        }

        var linkSelf = makeLinkTo("v1", "tasks", entity.getId()).withSelfRel();
        response.link(linkSelf);
        response.entity(task.build());

        return response.build();

    }

    private EntityModel<TasksDto.SubTask> toEntityModel(TaskEntity entity) {
        var task = TasksDto.SubTask.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .build();

        var linkSelf = makeLinkTo("v1", "tasks", entity.getId()).withSelfRel();

        return EntityModel.of(task).add(linkSelf);
    }

    private EntityModel<TagsDto.Details> toEntityModel(TagEntity entity) {
        var tag = TagsDto.Details.builder()
                .id(entity.getId())
                .name(entity.getName())
                .build();

        var linkSelf = makeLinkTo("v1", "tags", entity.getId()).withSelfRel();

        return EntityModel.of(tag).add(linkSelf);
    }

}