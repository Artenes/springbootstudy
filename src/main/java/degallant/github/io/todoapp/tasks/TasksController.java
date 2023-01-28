package degallant.github.io.todoapp.tasks;

import degallant.github.io.todoapp.users.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * @noinspection ClassCanBeRecord
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/tasks")
public class TasksController {

    private final TasksRepository tasksRepository;
    private final ListTasksService listService;
    private final DetailsTaskService detailService;
    private final CreateTasksService createService;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody TasksDto.Create request, Authentication authentication) {

        var uri = createService.create(request, authentication);

        return ResponseEntity.created(uri).build();

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
                authentication
        );

        return ResponseEntity.ok(response);

    }

    @GetMapping("/{id}")
    public ResponseEntity<?> details(@PathVariable String id, Authentication authentication) {

        RepresentationModel<?> response = detailService.details(id, authentication);

        return ResponseEntity.ok(response);

    }

}
