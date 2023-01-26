package degallant.github.io.todoapp.tasks;

import degallant.github.io.todoapp.common.SortingParser;
import degallant.github.io.todoapp.projects.ProjectsController;
import degallant.github.io.todoapp.projects.ProjectsDto;
import degallant.github.io.todoapp.projects.ProjectsRepository;
import degallant.github.io.todoapp.tags.TagEntity;
import degallant.github.io.todoapp.tags.TagsController;
import degallant.github.io.todoapp.tags.TagsDto;
import degallant.github.io.todoapp.tags.TagsRepository;
import degallant.github.io.todoapp.users.UserEntity;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.mediatype.hal.HalModelBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static degallant.github.io.todoapp.common.LinkBuilder.makeLink;
import static degallant.github.io.todoapp.common.LinkBuilder.makeLinkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/tasks")
public class TasksController {

    private final TasksRepository tasksRepository;
    private final TagsRepository tagsRepository;
    private final ProjectsRepository projectsRepository;
    private final SortingParser sortingParser;

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody TasksDto.Create request, Authentication authentication) {
        //tags are created beforehand
        //so we just query its instances to then pass in the to do entity below
        List<TagEntity> tags = Collections.emptyList();
        if (request.getTagsIds() != null && !request.getTagsIds().isEmpty()) {
            tags = tagsRepository.findAllById(request.getTagsIds());
        }

        var userId = ((UserEntity) authentication.getPrincipal()).getId();

        var entity = TaskEntity.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .dueDate(request.getDueDate())
                .priority(request.getPriority())
                .tags(tags)
                .parentId(request.getParentId())
                .userId(userId)
                .projectId(request.getProjectId())
                .build();

        entity = tasksRepository.save(entity);

        var link = linkTo(methodOn(getClass()).details(entity.getId(), authentication)).withSelfRel();

        return ResponseEntity.created(link.toUri()).build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> patch(@PathVariable UUID id, @RequestBody TasksDto.Update request, Authentication authentication) {
        UUID userId = ((UserEntity) authentication.getPrincipal()).getId();
        var entity = tasksRepository.findByIdAndUserId(id, userId).orElseThrow();

        if (request.getComplete() != null) {
            entity.setComplete(request.getComplete());
        }

        if (request.getProjectId() != null) {
            entity.setProjectId(request.getProjectId());
        }

        tasksRepository.save(entity);

        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<?> list(
            @RequestParam(name = "p", defaultValue = "1") int pageNumber,
            @RequestParam(name = "s", required = false) String sort,
            Authentication authentication
    ) {

        var response = HalModelBuilder.emptyHalModel();
        var userId = ((UserEntity) authentication.getPrincipal()).getId();

        var linkBuilder = makeLink("v1", "tasks").addSort(sort);
        var sortInformation = sortingParser.parse(sort, "title", "due_date");
        var pageRequest = PageRequest.of(pageNumber - 1, 10, sortInformation);
        var page = tasksRepository.findByUserId(userId, pageRequest);

        response.entity(TasksDto.Page.builder()
                .count(page.getNumberOfElements())
                .pages(page.getTotalPages())
                .total(page.getTotalElements())
                .build());

        response.link(linkBuilder.addPage(pageNumber).build().withSelfRel());

        if (page.hasNext()) {
            response.link(linkBuilder.addPage(pageNumber + 1).build().withRel("next"));
        }

        if (page.hasPrevious()) {
            response.link(linkBuilder.addPage(pageNumber - 1).build().withRel("previous"));
        }

        if (!page.isEmpty()) {
            response.link(linkBuilder.addPage(1).build().withRel("first"));
            response.link(linkBuilder.addPage(page.getTotalPages()).build().withRel("last"));
        }

        var tasks = page
                .stream()
                .map(entity -> {
                    var task = TasksDto.DetailsSimple.builder()
                            .id(entity.getId())
                            .title(entity.getTitle())
                            .description(entity.getDescription())
                            .dueDate(entity.getDueDate())
                            .build();
                    var linkSelf = makeLinkTo("v1", "tasks", entity.getId()).withSelfRel();
                    var linkComments = makeLinkTo("v1", "tasks", entity.getId(), "comments").withRel("comments");
                    return EntityModel.of(task).add(linkSelf, linkComments);
                })
                .collect(Collectors.toList());

        response.embed(tasks, TasksDto.DetailsSimple.class);

        return ResponseEntity.ok(response.build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> details(@PathVariable UUID id, Authentication authentication) {
        var userId = ((UserEntity) authentication.getPrincipal()).getId();
        var entity = tasksRepository.findByIdAndUserId(id, userId).orElseThrow();

        var task = TasksDto.DetailsComplete.builder()
                .title(entity.getTitle())
                .description(entity.getDescription())
                .dueDate(entity.getDueDate())
                .priority(entity.getPriority())
                .complete(entity.isComplete());

        var response = HalModelBuilder.emptyHalModel();

        if (entity.getTags() != null && !entity.getTags().isEmpty()) {
            var tags = entity.getTags()
                    .stream()
                    .map(tagEntity -> toEntityModel(tagEntity, authentication))
                    .collect(Collectors.toList());
            response.embed(tags);
        }

        var children = tasksRepository.findByParentId(entity.getId());
        if (children != null && !children.isEmpty()) {
            var subTasks = children
                    .stream()
                    .map(subTaskEntity -> toEntityModel(subTaskEntity, authentication))
                    .collect(Collectors.toList());
            response.embed(subTasks);
        }

        if (entity.getParentId() != null) {
            var parentEntity = tasksRepository.findByIdAndUserId(entity.getParentId(), userId).orElseThrow();
            var parent = TasksDto.ParentTask.builder()
                    .id(parentEntity.getId())
                    .title(parentEntity.getTitle())
                    .build();
            var linkSelf = linkTo(methodOn(getClass()).details(parentEntity.getId(), authentication)).withSelfRel();
            response.embed(EntityModel.of(parent).add(linkSelf));
        }

        if (entity.getProjectId() != null) {
            var projectEntity = projectsRepository.findByIdAndUserId(entity.getProjectId(), userId).orElseThrow();
            var project = ProjectsDto.Details.builder()
                    .id(projectEntity.getId())
                    .title(projectEntity.getTitle())
                    .build();
            var linkSelf = linkTo(methodOn(ProjectsController.class).details(projectEntity.getId(), authentication)).withSelfRel();
            response.embed(EntityModel.of(project).add(linkSelf));
        }

        var linkSelf = linkTo(methodOn(getClass()).details(entity.getId(), authentication)).withSelfRel();
        response.link(linkSelf);
        response.entity(task.build());

        return ResponseEntity.ok(response.build());
    }

    private EntityModel<TasksDto.SubTask> toEntityModel(TaskEntity entity, Authentication authentication) {
        var task = TasksDto.SubTask.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .build();

        var linkSelf = linkTo(methodOn(getClass()).details(entity.getId(), authentication)).withSelfRel();

        return EntityModel.of(task).add(linkSelf);
    }

    private EntityModel<TagsDto.Details> toEntityModel(TagEntity entity, Authentication authentication) {
        var tag = TagsDto.Details.builder()
                .id(entity.getId())
                .name(entity.getName())
                .build();

        var linkSelf = linkTo(methodOn(TagsController.class).details(entity.getId(), authentication)).withSelfRel();

        return EntityModel.of(tag).add(linkSelf);
    }

}
