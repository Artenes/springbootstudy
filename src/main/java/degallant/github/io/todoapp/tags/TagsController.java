package degallant.github.io.todoapp.tags;

import degallant.github.io.todoapp.common.LinkBuilder;
import degallant.github.io.todoapp.users.UserEntity;
import degallant.github.io.todoapp.sanitization.parsers.PrimitiveFieldParser;
import degallant.github.io.todoapp.sanitization.FieldValidator;
import degallant.github.io.todoapp.sanitization.Sanitizer;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.mediatype.hal.HalModelBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @noinspection ClassCanBeRecord
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/tags")
public class TagsController {

    private final TagsRepository repository;
    private final Sanitizer sanitizer;
    private final FieldValidator rules;
    private final PrimitiveFieldParser parser;
    private final LinkBuilder link;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody TagsDto.Create request, Authentication authentication) {

        var result = sanitizer.sanitize(
                sanitizer.field("name").withRequiredValue(request.getName()).sanitize(value -> {
                    rules.isNotEmpty(value);
                    return value;
                })
        );

        var userId = ((UserEntity) authentication.getPrincipal()).getId();

        var entity = TagEntity.builder()
                .name(result.get("name").value())
                .userId(userId)
                .build();

        entity = repository.save(entity);

        var linkCreated = link.to("tags").slash(entity.getId()).withSelfRel();

        return ResponseEntity.created(linkCreated.toUri()).build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> details(@PathVariable String id, Authentication authentication) {
        var userId = ((UserEntity) authentication.getPrincipal()).getId();
        var entity = repository.findByIdAndUserId(parser.toUuidOrThrow(id), userId).orElseThrow();
        var response = toEntityModel(entity);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<?> list(Authentication authentication) {
        UUID userId = ((UserEntity) authentication.getPrincipal()).getId();

        var tags = repository.findByUserId(userId)
                .stream()
                .map(this::toEntityModel)
                .collect(Collectors.toList());

        var selfRef = link.to("tags").withSelfRel();

        var response = HalModelBuilder.emptyHalModel()
                .embed(tags, TagsDto.Details.class)
                .link(selfRef).build();

        return ResponseEntity.ok(response);
    }

    private EntityModel<TagsDto.Details> toEntityModel(TagEntity entity) {
        var linkSelf = link.to("tags").slash(entity.getId()).withSelfRel();
        var linkAll = link.to("tags").withRel("all");

        var tag = TagsDto.Details.builder()
                .id(entity.getId())
                .name(entity.getName())
                .build();

        return EntityModel.of(tag).add(linkSelf, linkAll);
    }

}
