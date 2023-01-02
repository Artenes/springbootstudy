package degallant.github.io.todoapp;

import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<?> create(@RequestBody TodoDto.Create request) throws URISyntaxException {

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
                .build();

        todoEntity = todoRepository.save(todoEntity);

        var link = linkTo(TodoController.class).slash(todoEntity.getId());

        return ResponseEntity.created(link.toUri()).build();

    }

    @GetMapping
    public List<TodoEntity> index() {

        return todoRepository.findAll();

    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> patch(@PathVariable UUID id, @RequestBody TodoDto.PatchComplete request) {

        var todoEntity = todoRepository.findById(id).orElseThrow();

        todoEntity.setComplete(request.complete());

        todoRepository.save(todoEntity);

        return ResponseEntity.ok().build();

    }

    @GetMapping("/{id}")
    public TodoDto.Details get(@PathVariable UUID id) {

        var todo = todoRepository.findById(id).orElseThrow();

        var tags = todo.getTags().stream()
                .map(tag -> TagDto.Details.builder()
                        .name(tag.getName())
                        .uuid(tag.getId())
                        .build())
                .collect(Collectors.toList());

        return TodoDto.Details.builder()
                .title(todo.getTitle())
                .description(todo.getDescription())
                .dueDate(todo.getDueDate())
                .priority(todo.getPriority())
                .tags(tags)
                .build();

    }

}
