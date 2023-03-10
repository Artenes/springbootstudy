package degallant.github.io.todoapp.domain.tasks;

import degallant.github.io.todoapp.OffsetHolder;
import degallant.github.io.todoapp.common.LinkBuilder;
import degallant.github.io.todoapp.domain.projects.ProjectsDto;
import degallant.github.io.todoapp.domain.tags.TagEntity;
import degallant.github.io.todoapp.domain.tags.TagsDto;
import degallant.github.io.todoapp.domain.users.UserEntity;
import degallant.github.io.todoapp.sanitization.parsers.TasksFieldParser;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.mediatype.hal.HalModelBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

/**
 * @noinspection ClassCanBeRecord
 */
@Service
@RequiredArgsConstructor
public class DetailsTaskService {

    private final TasksFieldParser taskParser;
    private final LinkBuilder link;
    private final OffsetHolder offsetHolder;

    public RepresentationModel<?> details(String rawId, Authentication authentication) {

        var user = (UserEntity) authentication.getPrincipal();
        var entity = taskParser.toTaskOrThrowNoSuchElement(rawId, user);

        var task = TasksDto.DetailsComplete.builder()
                .title(entity.getTitle())
                .description(entity.getDescription())
                .dueDate(offsetHolder.applyTo(entity.getDueDate()))
                .priority(entity.getPriority())
                .complete(entity.getComplete())
                .createdAt(offsetHolder.applyTo(entity.getCreatedAt()))
                .updatedAt(offsetHolder.applyTo(entity.getUpdatedAt()));

        var response = HalModelBuilder.emptyHalModel();

        if (entity.getTags() != null && !entity.getTags().isEmpty()) {
            var tags = entity.getTags()
                    .stream()
                    .map(this::toEntityModel)
                    .collect(Collectors.toList());
            response.embed(tags);
        }

        var children = entity.getSubTasks();
        if (children != null && !children.isEmpty()) {
            var subTasks = children
                    .stream()
                    .map(this::toEntityModel)
                    .collect(Collectors.toList());
            response.embed(subTasks);
        }

        if (entity.getParent() != null) {
            var parentEntity = entity.getParent();
            var parent = TasksDto.ParentTask.builder()
                    .id(parentEntity.getId())
                    .title(parentEntity.getTitle())
                    .build();
            var linkSelf = link.to("tasks").slash(parentEntity.getId()).withSelfRel();
            response.embed(EntityModel.of(parent).add(linkSelf));
        }

        if (entity.getProject() != null) {
            var projectEntity = entity.getProject();
            var project = ProjectsDto.Details.builder()
                    .id(projectEntity.getId())
                    .title(projectEntity.getTitle())
                    .build();
            var linkSelf = link.to("projects").slash(projectEntity.getId()).withSelfRel();
            response.embed(EntityModel.of(project).add(linkSelf));
        }

        var linkSelf = link.to("tasks").slash(entity.getId()).withSelfRel();
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

        var linkSelf = link.to("tasks").slash(entity.getId()).withSelfRel();

        return EntityModel.of(task).add(linkSelf);
    }

    private EntityModel<TagsDto.Details> toEntityModel(TagEntity entity) {
        var tag = TagsDto.Details.builder()
                .id(entity.getId())
                .name(entity.getName())
                .build();

        var linkSelf = link.to("tags").slash(entity.getId()).withSelfRel();

        return EntityModel.of(tag).add(linkSelf);
    }

}
