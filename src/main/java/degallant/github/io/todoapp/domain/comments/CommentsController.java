package degallant.github.io.todoapp.domain.comments;

import degallant.github.io.todoapp.OffsetHolder;
import degallant.github.io.todoapp.common.LinkBuilder;
import degallant.github.io.todoapp.domain.users.UserEntity;
import degallant.github.io.todoapp.sanitization.FieldValidator;
import degallant.github.io.todoapp.sanitization.Sanitizer;
import degallant.github.io.todoapp.sanitization.parsers.CommentsFieldParser;
import degallant.github.io.todoapp.sanitization.parsers.TasksFieldParser;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.mediatype.hal.HalModelBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.stream.Collectors;

/**
 * @noinspection ClassCanBeRecord, unused
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/tasks/{id}/comments")
public class CommentsController {

    private final CommentsRepository commentsRepository;
    private final Sanitizer sanitizer;
    private final FieldValidator rules;
    private final TasksFieldParser taskParser;
    private final CommentsFieldParser commentParser;
    private final LinkBuilder link;
    private final OffsetHolder offsetHolder;

    @PostMapping
    public ResponseEntity<?> create(@PathVariable String id, @RequestBody CommentsDto.Create request, Authentication authentication) {

        var user = (UserEntity) authentication.getPrincipal();
        var task = taskParser.toTaskOrThrowNoSuchElement(id, user);

        var result = sanitizer.sanitize(
                sanitizer.field("text").withRequiredValue(request.getText()).sanitize(value -> {
                    rules.isNotEmpty(value);
                    return value;
                })
        );

        var entity = CommentEntity.builder()
                .text(result.get("text").value())
                .commenter(user)
                .task(task)
                .build();

        entity = commentsRepository.save(entity);

        var linkSelf = link.to("tasks")
                .slash(task.getId()).slash("comments").slash(entity.getId())
                .withSelfRel();

        return ResponseEntity.created(linkSelf.toUri()).build();

    }

    @GetMapping("/{commentId}")
    public ResponseEntity<?> details(@PathVariable String id, @PathVariable String commentId, Authentication authentication) {

        var user = (UserEntity) authentication.getPrincipal();
        var comment = commentParser.toCommentOrThrowNoSuchElement(id, commentId, user);

        var response = toEntityModel(comment);

        return ResponseEntity.ok(response);

    }

    @PatchMapping("/{commentId}")
    public ResponseEntity<?> patch(@PathVariable String id, @PathVariable String commentId, @RequestBody CommentsDto.Patch request, Authentication authentication) {

        var user = (UserEntity) authentication.getPrincipal();
        var comment = commentParser.toCommentOrThrowNoSuchElement(id, commentId, user);

        var result = sanitizer.sanitize(
                sanitizer.field("text").withOptionalValue(request.getText()).sanitize(value -> {
                    rules.isNotEmpty(value);
                    return value;
                })
        );

        if (!result.hasAnyFieldWithValue()) {
            return ResponseEntity.noContent().build();
        }

        result.get("text").consumeIfExists(comment::setText);
        commentsRepository.save(comment);

        return ResponseEntity.ok().build();

    }

    @GetMapping
    public ResponseEntity<?> list(@PathVariable String id, Authentication authentication) {

        var user = (UserEntity) authentication.getPrincipal();
        var task = taskParser.toTaskOrThrowNoSuchElement(id, user);
        var entities = task.getComments();

        var comments = entities.stream()
                .map(this::toEntityModel)
                .collect(Collectors.toList());

        var linkSelf = link.to("tasks").slash(task.getId()).slash("comments").withSelfRel();
        var response = HalModelBuilder.emptyHalModel()
                .embed(comments, CommentsDto.Details.class)
                .link(linkSelf)
                .build();

        return ResponseEntity.ok(response);

    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<?> delete(@PathVariable String id, @PathVariable String commentId, Authentication authentication) {

        var user = (UserEntity) authentication.getPrincipal();
        var comment = commentParser.toCommentOrThrowNoSuchElement(id, commentId, user);

        comment.setDeletedAt(OffsetDateTime.now());
        commentsRepository.save(comment);

        return ResponseEntity.noContent().build();

    }

    private EntityModel<CommentsDto.Details> toEntityModel(CommentEntity entity) {
        var comment = CommentsDto.Details.builder()
                .id(entity.getId())
                .text(entity.getText())
                .commentedAt(offsetHolder.applyTo(entity.getCreatedAt()))
                .editedAt(offsetHolder.applyTo(entity.getUpdatedAt()))
                .build();
        var linkSelf = link.to("tasks").slash(entity.getTask().getId()).slash("comments").slash(entity.getId()).withSelfRel();
        var linkAll = link.to("tasks").slash(entity.getTask().getId()).slash("comments").withRel("all");
        var linkTask = link.to("tasks").slash(entity.getTask().getId()).withRel("task");
        return EntityModel.of(comment).add(linkSelf, linkAll, linkTask);
    }

}
