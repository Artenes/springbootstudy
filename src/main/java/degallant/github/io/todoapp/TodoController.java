package degallant.github.io.todoapp;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/todo")
public class TodoController {

    private final TodoRepository repository;

    public TodoController(TodoRepository repository) {
        this.repository = repository;
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody TodoDto.Create request) throws URISyntaxException {

        var todoEntity = TodoEntity.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .dueDate(request.getDueDate())
                .priority(request.getPriority())
                .build();

        todoEntity = repository.save(todoEntity);

        return ResponseEntity.created(new URI("/v1/todo/" + todoEntity.getId())).build();

    }

    @GetMapping
    public List<TodoEntity> index() {

        return repository.findAll();

    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> patch(@PathVariable UUID id, @RequestBody TodoDto.PatchComplete request) {

        var todoEntity = repository.findById(id).orElseThrow();

        todoEntity.setComplete(request.complete());

        repository.save(todoEntity);

        return ResponseEntity.ok().build();

    }

    @GetMapping("/{id}")
    public TodoDto.Details get(@PathVariable UUID id) {

        var todo = repository.findById(id).orElseThrow();

        return TodoDto.Details.builder()
                .title(todo.getTitle())
                .description(todo.getDescription())
                .dueDate(todo.getDueDate())
                .priority(todo.getPriority())
                .build();

    }

}
