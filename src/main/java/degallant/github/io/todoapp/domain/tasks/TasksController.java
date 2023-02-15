package degallant.github.io.todoapp.domain.tasks;

import degallant.github.io.todoapp.domain.users.UserEntity;
import degallant.github.io.todoapp.sanitization.parsers.TasksFieldParser;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;

/**
 * @noinspection ClassCanBeRecord, unused
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/tasks")
public class TasksController {

    private final ListTasksService listService;
    private final DetailsTaskService detailService;
    private final CreateTasksService createService;
    private final PatchTasksService patchService;
    private final TasksRepository repository;
    private final TasksFieldParser parser;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody TasksDto.Create request, Authentication authentication) {

        var uri = createService.create(request, (UserEntity) authentication.getPrincipal());

        return ResponseEntity.created(uri).build();

    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> patch(@PathVariable String id, @RequestBody TasksDto.Create request, Authentication authentication) {

        patchService.patch(id, request, (UserEntity) authentication.getPrincipal());

        return ResponseEntity.ok().build();

    }

    @GetMapping
    public ResponseEntity<?> list(
            @RequestParam(name = "p", defaultValue = "1") String requestedPageNumber,
            @RequestParam(name = "s", required = false) String sort,
            @RequestParam(required = false) String title,
            @RequestParam(name = "due_date", required = false) String dueDate,
            @RequestParam(name = "complete", required = false) String requestedComplete,
            Authentication authentication
    ) {

        RepresentationModel<?> response = listService.list(
                requestedPageNumber,
                sort,
                title,
                dueDate,
                requestedComplete,
                (UserEntity) authentication.getPrincipal()
        );

        return ResponseEntity.ok(response);

    }

    @GetMapping("/{id}")
    public ResponseEntity<?> details(@PathVariable String id, Authentication authentication) {

        RepresentationModel<?> response = detailService.details(id, authentication);

        return ResponseEntity.ok(response);

    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable String id, Authentication authentication) {

        var user = (UserEntity) authentication.getPrincipal();
        var task = parser.toTaskOrThrowNoSuchElement(id, user);

        task.setDeletedAt(OffsetDateTime.now());
        repository.save(task);

        return ResponseEntity.noContent().build();

    }

}
