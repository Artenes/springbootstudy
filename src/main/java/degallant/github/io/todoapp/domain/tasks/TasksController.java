package degallant.github.io.todoapp.domain.tasks;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import degallant.github.io.todoapp.OffsetHolder;
import degallant.github.io.todoapp.domain.users.UserEntity;
import degallant.github.io.todoapp.sanitization.parsers.TasksFieldParser;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.mediatype.MessageResolver;
import org.springframework.hateoas.mediatype.hal.CurieProvider;
import org.springframework.hateoas.mediatype.hal.Jackson2HalModule;
import org.springframework.hateoas.server.core.AnnotationLinkRelationProvider;
import org.springframework.hateoas.server.core.DelegatingLinkRelationProvider;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.OffsetDateTime;

/**
 * @noinspection ClassCanBeRecord, unused, ConstantConditions
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
    private final CacheManager cacheManager;
    private final ObjectMapper mapper;
    private final OffsetHolder offsetHolder;
    private final MeterRegistry meterRegistry;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody TasksDto.Create request, Authentication authentication) {

        var user = (UserEntity) authentication.getPrincipal();
        var uri = createService.create(request, user);

        invalidateCacheList(user);

        return ResponseEntity.created(uri).build();

    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> patch(@PathVariable String id, @RequestBody TasksDto.Create request, Authentication authentication) {

        var user = (UserEntity) authentication.getPrincipal();

        if (patchService.patch(id, request, user)) {
            invalidateCacheList(user);
            return ResponseEntity.ok().build();
        }

        return ResponseEntity.noContent().build();

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

        meterRegistry.counter("PAGE_VIEW.TasksList").increment();

        var startTime = System.currentTimeMillis();
        var timer = meterRegistry.timer("execution.time.TasksList");

        var user = (UserEntity) authentication.getPrincipal();

        var cache = cacheManager.getCache("user:" + user.getId() + ":tasks");
        var cacheId = "offset=" + offsetHolder.getOffset() + "&page=" + requestedPageNumber + "&sort=" + sort + "&title=" + title + "&dueDate=" + dueDate + "&complete=" + requestedComplete;
        var cachedValue = cache.get(cacheId, String.class);

        if (cachedValue != null) {
            timer.record(Duration.ofMillis(System.currentTimeMillis() - startTime));
            return ResponseEntity.ok().contentType(MediaType.valueOf("application/hal+json")).body(cachedValue);
        }

        RepresentationModel<?> response = listService.list(
                requestedPageNumber,
                sort,
                title,
                dueDate,
                requestedComplete,
                user
        );

        var serialized = serialize(response);
        cache.put(cacheId, serialized);

        timer.record(Duration.ofMillis(System.currentTimeMillis() - startTime));
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

        invalidateCacheList(user);

        return ResponseEntity.noContent().build();

    }

    private String serialize(Object model) {
        try {
            var provider = new DelegatingLinkRelationProvider(new AnnotationLinkRelationProvider());
            var instantiator = new Jackson2HalModule.HalHandlerInstantiator(provider, CurieProvider.NONE, MessageResolver.DEFAULTS_ONLY);
            mapper.registerModule(new Jackson2HalModule());
            mapper.setHandlerInstantiator(instantiator);
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(model);
        } catch (JsonProcessingException exception) {
            throw new RuntimeException(exception);
        }
    }

    private void invalidateCacheList(UserEntity user) {
        var cache = cacheManager.getCache("user:" + user.getId() + ":tasks");
        cache.invalidate();
    }

}
