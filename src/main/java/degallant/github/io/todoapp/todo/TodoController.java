package degallant.github.io.todoapp.todo;

import degallant.github.io.todoapp.tag.TagDto;
import degallant.github.io.todoapp.tag.TagEntity;
import degallant.github.io.todoapp.tag.TagRepository;
import degallant.github.io.todoapp.user.UserEntity;
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
@RequestMapping("/v1/todo")
public class TodoController {

    private final TodoRepository todoRepository;

    private final TagRepository tagRepository;

    public TodoController(TodoRepository repository, TagRepository tagRepository) {
        this.todoRepository = repository;
        this.tagRepository = tagRepository;
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody TodoDto.Create request, Authentication authentication) throws URISyntaxException {

        //todo this doesn't make sense
        List<TagEntity> tags = Collections.emptyList();
        if (request.getTags() != null && !request.getTags().isEmpty()) {
            tags = tagRepository.findAllById(request.getTags());
        }

        var todoEntity = TodoEntity.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .dueDate(request.getDueDate())
                .priority(request.getPriority())
                .tags(tags)
                .parent(request.getParent())
                .userId(((UserEntity) authentication.getPrincipal()).getId())
                .build();

        todoEntity = todoRepository.save(todoEntity);

        var link = linkTo(TodoController.class).slash(todoEntity.getId());

        return ResponseEntity.created(link.toUri()).build();

    }

    @GetMapping
    public List<TodoEntity> index(Authentication authentication) {

        return todoRepository.findByUserId(((UserEntity) authentication.getPrincipal()).getId());

    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> patch(@PathVariable UUID id, @RequestBody TodoDto.PatchComplete request, Authentication authentication) {

        var todoEntity = todoRepository.findByIdAndUserId(id, ((UserEntity) authentication.getPrincipal()).getId()).orElseThrow();

        todoEntity.setComplete(request.complete());

        todoRepository.save(todoEntity);

        return ResponseEntity.ok().build();

    }

    @GetMapping("/{id}")
    public TodoDto.Details get(@PathVariable UUID id, Authentication authentication) {

        var todo = todoRepository.findByIdAndUserId(id, ((UserEntity) authentication.getPrincipal()).getId()).orElseThrow();

        var response = TodoDto.Details.builder()
                .title(todo.getTitle())
                .description(todo.getDescription())
                .dueDate(todo.getDueDate())
                .priority(todo.getPriority());

        if (todo.getTags() != null && !todo.getTags().isEmpty()) {
            response.tags(todo.getTags().stream()
                    .map(tag -> TagDto.Details.builder()
                            .name(tag.getName())
                            .uuid(tag.getId())
                            .build())
                    .collect(Collectors.toList()));
        }

        var children = todoRepository.findByParent(todo.getId());
        if (children != null && !children.isEmpty()) {
            response.children(children.stream()
                    .map(child -> linkTo(TodoController.class).slash(child.getId()).toUri())
                    .collect(Collectors.toList()));
        }

        if (todo.getParent() != null) {
            response.parent(linkTo(TodoController.class).slash(todo.getParent()).toUri());
        }

        return response.build();

    }

}
