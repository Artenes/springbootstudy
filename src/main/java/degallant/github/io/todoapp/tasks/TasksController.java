package degallant.github.io.todoapp.tasks;

import degallant.github.io.todoapp.projects.ProjectsController;
import degallant.github.io.todoapp.tags.TagsDto;
import degallant.github.io.todoapp.tags.TagEntity;
import degallant.github.io.todoapp.tags.TagsRepository;
import degallant.github.io.todoapp.users.UserEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@RestController
@RequestMapping("/v1/tasks")
public class TasksController {

    private final TasksRepository tasksRepository;

    private final TagsRepository tagsRepository;

    public TasksController(TasksRepository repository, TagsRepository tagsRepository) {
        this.tasksRepository = repository;
        this.tagsRepository = tagsRepository;
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody TasksDto.Create request, Authentication authentication) throws URISyntaxException {

        //tags are created beforehand
        //so we just query its instances to then pass in the to do entity below
        List<TagEntity> tags = Collections.emptyList();
        if (request.getTags() != null && !request.getTags().isEmpty()) {
            tags = tagsRepository.findAllById(request.getTags());
        }

        var taskEntity = TaskEntity.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .dueDate(request.getDueDate())
                .priority(request.getPriority())
                .tags(tags)
                .parent(request.getParent())
                .userId(((UserEntity) authentication.getPrincipal()).getId())
                .projectId(request.getProject())
                .build();

        taskEntity = tasksRepository.save(taskEntity);

        var link = linkTo(TasksController.class).slash(taskEntity.getId());

        return ResponseEntity.created(link.toUri()).build();

    }

    @GetMapping
    public List<TaskEntity> list(Authentication authentication) {

        return tasksRepository.findByUserId(((UserEntity) authentication.getPrincipal()).getId());

    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> patch(@PathVariable UUID id, @RequestBody TasksDto.Update request, Authentication authentication) {

        var taskEntity = tasksRepository.findByIdAndUserId(id, ((UserEntity) authentication.getPrincipal()).getId()).orElseThrow();

        if (request.getComplete() != null) {
            taskEntity.setComplete(request.getComplete());
        }

        if (request.getProjectId() != null) {
            taskEntity.setProjectId(request.getProjectId());
        }

        tasksRepository.save(taskEntity);

        return ResponseEntity.ok().build();

    }

    @GetMapping("/{id}")
    public TasksDto.Details get(@PathVariable UUID id, Authentication authentication) {

        var task = tasksRepository.findByIdAndUserId(id, ((UserEntity) authentication.getPrincipal()).getId()).orElseThrow();

        var response = TasksDto.Details.builder()
                .title(task.getTitle())
                .description(task.getDescription())
                .dueDate(task.getDueDate())
                .priority(task.getPriority());

        if (task.getTags() != null && !task.getTags().isEmpty()) {
            response.tags(task.getTags().stream()
                    .map(tag -> TagsDto.Details.builder()
                            .name(tag.getName())
                            .id(tag.getId())
                            .build())
                    .collect(Collectors.toList()));
        }

        var children = tasksRepository.findByParent(task.getId());
        if (children != null && !children.isEmpty()) {
            response.children(children.stream()
                    .map(child -> linkTo(TasksController.class).slash(child.getId()).toUri())
                    .collect(Collectors.toList()));
        }

        if (task.getParent() != null) {
            response.parent(linkTo(TasksController.class).slash(task.getParent()).toUri());
        }

        if (task.getProjectId() != null) {
            response.project(linkTo(ProjectsController.class).slash(task.getProjectId()).toUri());
        }

        return response.build();

    }

}
