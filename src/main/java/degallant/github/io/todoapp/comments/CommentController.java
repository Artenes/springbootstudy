package degallant.github.io.todoapp.comments;

import degallant.github.io.todoapp.tasks.TasksRepository;
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
@RequestMapping("/v1/tasks/{id}/comments")
public class CommentController {

    private final TasksRepository tasksRepository;

    private final CommentRepository repository;

    @PostMapping
    public ResponseEntity<?> create(@PathVariable UUID id, @RequestBody CommentTdo.Create request, Authentication authentication) {

        UUID userId = ((UserEntity) authentication.getPrincipal()).getId();
        tasksRepository.findByIdAndUserId(id, userId).orElseThrow();

        var entity = CommentEntity.builder()
                .text(request.getText())
                .userId(userId)
                .taskId(id)
                .build();

        entity = repository.save(entity);

        var link = linkTo(CommentController.class, id);

        return ResponseEntity.created(link.toUri()).build();

    }

    @GetMapping
    public List<CommentEntity> index(@PathVariable UUID id, Authentication authentication) {

        UUID userId = ((UserEntity) authentication.getPrincipal()).getId();
        tasksRepository.findByIdAndUserId(id, userId).orElseThrow();

        return repository.findByTaskIdAndUserId(id, userId);

    }

}
