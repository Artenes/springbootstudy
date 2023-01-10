package degallant.github.io.todoapp.comments;

import degallant.github.io.todoapp.todo.TodoRepository;
import degallant.github.io.todoapp.user.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/todo/{id}/comments")
public class CommentController {

    private final TodoRepository todoRepository;

    private final CommentRepository repository;

    @PostMapping
    public ResponseEntity<?> create(@PathVariable UUID id, @RequestBody CommentTdo.Create request, Authentication authentication) {

        UUID userId = ((UserEntity) authentication.getPrincipal()).getId();
        todoRepository.findByIdAndUserId(id, userId).orElseThrow();

        var entity = CommentEntity.builder()
                .text(request.getText())
                .userId(userId)
                .todoId(id)
                .build();

        entity = repository.save(entity);

        var link = linkTo(CommentController.class, id);

        return ResponseEntity.created(link.toUri()).build();

    }

    @GetMapping
    public List<CommentEntity> index(@PathVariable UUID id, Authentication authentication) {

        UUID userId = ((UserEntity) authentication.getPrincipal()).getId();
        todoRepository.findByIdAndUserId(id, userId).orElseThrow();

        return repository.findByTodoIdAndUserId(id, userId);

    }

}
