package degallant.github.io.todoapp.comments;

import degallant.github.io.todoapp.tasks.TasksController;
import degallant.github.io.todoapp.tasks.TasksRepository;
import degallant.github.io.todoapp.users.UserEntity;
import degallant.github.io.todoapp.validation.ValidationRules;
import degallant.github.io.todoapp.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.mediatype.hal.HalModelBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.UUID;
import java.util.stream.Collectors;

import static degallant.github.io.todoapp.validation.Validation.field;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/** @noinspection ClassCanBeRecord*/
@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/tasks/{id}/comments")
public class CommentsController {

    private final TasksRepository tasksRepository;
    private final CommentsRepository commentsRepository;
    private final Validator validator;
    private final ValidationRules rules;

    @PostMapping
    public ResponseEntity<?> create(@PathVariable UUID id, @RequestBody CommentsDto.Create request, Authentication authentication) {

        validator.validate(
                field("text", request.getText(), rules.isNotEmpty(), true)
        );

        var userId = ((UserEntity) authentication.getPrincipal()).getId();

        //guard
        tasksRepository.findByIdAndUserId(id, userId).orElseThrow();

        var entity = CommentEntity.builder()
                .text(request.getText())
                .userId(userId)
                .taskId(id)
                .build();

        entity = commentsRepository.save(entity);

        var link = linkTo(methodOn(getClass(), id).details(id, entity.getId(), authentication)).withSelfRel();

        return ResponseEntity.created(link.toUri()).build();
    }

    @GetMapping("/{commentId}")
    public ResponseEntity<?> details(@PathVariable UUID id, @PathVariable UUID commentId, Authentication authentication) {
        var userId = ((UserEntity) authentication.getPrincipal()).getId();
        var entity = commentsRepository.findByIdAndUserId(commentId, userId).orElseThrow();

        var response = toEntityModel(entity, authentication);

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public RepresentationModel<?> list(@PathVariable UUID id, Authentication authentication) {
        UUID userId = ((UserEntity) authentication.getPrincipal()).getId();

        //guard
        tasksRepository.findByIdAndUserId(id, userId).orElseThrow();

        var entities = commentsRepository.findByTaskIdAndUserId(id, userId);
        var comments = entities.stream()
                .map(entity -> toEntityModel(entity, authentication))
                .collect(Collectors.toList());

        var linkSelf = linkTo(methodOn(getClass()).list(id, authentication)).withSelfRel();

        if (comments.isEmpty()) {
            return HalModelBuilder.emptyHalModel()
                    .embed(Collections.emptyList(), CommentsDto.Details.class)
                    .link(linkSelf)
                    .build();
        }

        return CollectionModel.of(comments).add(linkSelf);
    }

    private EntityModel<CommentsDto.Details> toEntityModel(CommentEntity entity, Authentication authentication) {
        var comment = CommentsDto.Details.builder().id(entity.getId()).text(entity.getText()).build();
        var linkSelf = linkTo(methodOn(getClass()).details(entity.getTaskId(), entity.getId(), authentication)).withSelfRel();
        var linkAll = linkTo(methodOn(getClass()).list(entity.getTaskId(), authentication)).withRel("all");
        var linkTask = linkTo(methodOn(TasksController.class).details(entity.getTaskId().toString(), authentication)).withRel("task");
        return EntityModel.of(comment).add(linkSelf, linkAll, linkTask);
    }

}
