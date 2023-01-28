package degallant.github.io.todoapp.tags;

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

/**
 * @noinspection ClassCanBeRecord
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/tags")
public class TagsController {

    private final TagsRepository repository;
    private final Validator validator;
    private final ValidationRules rules;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody TagsDto.Create request, Authentication authentication) {

        validator.validate(
                field("name", request.getName(), rules.isNotEmpty(), true)
        );

        var userId = ((UserEntity) authentication.getPrincipal()).getId();

        var entity = TagEntity.builder()
                .name(request.getName())
                .userId(userId)
                .build();

        entity = repository.save(entity);

        var linkCreated = linkTo(methodOn(getClass()).details(entity.getId(), authentication)).withSelfRel();

        return ResponseEntity.created(linkCreated.toUri()).build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> details(@PathVariable UUID id, Authentication authentication) {
        var userId = ((UserEntity) authentication.getPrincipal()).getId();
        var entity = repository.findByIdAndUserId(id, userId).orElseThrow();
        var response = toEntityModel(entity, authentication);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public RepresentationModel<?> list(Authentication authentication) {
        UUID userId = ((UserEntity) authentication.getPrincipal()).getId();

        var tags = repository.findByUserId(userId)
                .stream()
                .map(entity -> toEntityModel(entity, authentication))
                .collect(Collectors.toList());

        var selfRef = linkTo(methodOn(getClass()).list(authentication)).withSelfRel();

        if (tags.isEmpty()) {
            return HalModelBuilder.emptyHalModel()
                    .embed(Collections.emptyList(), TagsDto.Details.class)
                    .link(selfRef).build();
        }

        return CollectionModel.of(tags).add(selfRef);
    }

    private EntityModel<TagsDto.Details> toEntityModel(TagEntity entity, Authentication authentication) {
        var linkSelf = linkTo(methodOn(getClass()).details(entity.getId(), authentication)).withSelfRel();
        var linkAll = linkTo(methodOn(getClass()).list(authentication)).withRel("all");

        var tag = TagsDto.Details.builder()
                .id(entity.getId())
                .name(entity.getName())
                .build();

        return EntityModel.of(tag).add(linkSelf, linkAll);
    }

}
