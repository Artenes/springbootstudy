package degallant.github.io.todoapp.comments;

import degallant.github.io.todoapp.tasks.TasksRepository;
import degallant.github.io.todoapp.users.UserEntity;
import degallant.github.io.todoapp.validation.FieldParser;
import degallant.github.io.todoapp.validation.FieldValidator;
import degallant.github.io.todoapp.validation.Sanitizer;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.mediatype.hal.HalModelBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

import static degallant.github.io.todoapp.common.LinkBuilder.makeLinkTo;

/**
 * @noinspection ClassCanBeRecord
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/tasks/{id}/comments")
public class CommentsController {

    private final TasksRepository tasksRepository;
    private final CommentsRepository commentsRepository;
    private final Sanitizer sanitizer;
    private final FieldValidator rules;
    private final FieldParser parser;

    @PostMapping
    public ResponseEntity<?> create(@PathVariable String id, @RequestBody CommentsDto.Create request, Authentication authentication) {

        var userId = ((UserEntity) authentication.getPrincipal()).getId();
        var task = parser.toTask(id, userId);

        var result = sanitizer.sanitize(
                sanitizer.field("text").withRequiredValue(request.getText()).sanitize(value -> {
                    rules.isNotEmpty(value);
                    return value;
                })
        );

        var entity = CommentEntity.builder()
                .text(result.get("text").value())
                .userId(userId)
                .taskId(task.getId())
                .build();

        entity = commentsRepository.save(entity);

        var link = makeLinkTo("v1", "tasks", task.getId(), "comments", entity.getId()).withSelfRel();

        return ResponseEntity.created(link.toUri()).build();
    }

    @GetMapping("/{commentId}")
    public ResponseEntity<?> details(@PathVariable String id, @PathVariable String commentId, Authentication authentication) {

        var userId = ((UserEntity) authentication.getPrincipal()).getId();
        var task = parser.toTask(id, userId);
        var comment = parser.toComment(commentId, task.getId(), userId);

        var response = toEntityModel(comment);

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<?> list(@PathVariable String id, Authentication authentication) {

        var userId = ((UserEntity) authentication.getPrincipal()).getId();
        var task = tasksRepository.findByIdAndUserId(parser.toUuidOrThrow(id), userId).orElseThrow();
        var entities = commentsRepository.findByTaskIdAndUserId(task.getId(), userId);

        var comments = entities.stream()
                .map(this::toEntityModel)
                .collect(Collectors.toList());

        var linkSelf = makeLinkTo("v1", "tasks", task.getId(), "comments").withSelfRel();
        var response = HalModelBuilder.emptyHalModel()
                .embed(comments, CommentsDto.Details.class)
                .link(linkSelf)
                .build();

        return ResponseEntity.ok(response);
    }

    private EntityModel<CommentsDto.Details> toEntityModel(CommentEntity entity) {
        var comment = CommentsDto.Details.builder().id(entity.getId()).text(entity.getText()).build();
        var linkSelf = makeLinkTo("v1", "tasks", entity.getTaskId(), "comments", entity.getId()).withSelfRel();
        var linkAll = makeLinkTo("v1", "tasks", entity.getTaskId(), "comments").withRel("all");
        var linkTask = makeLinkTo("v1", "tasks", entity.getTaskId()).withRel("task");
        return EntityModel.of(comment).add(linkSelf, linkAll, linkTask);
    }

}
