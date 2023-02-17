package degallant.github.io.todoapp.domain.tags;

import degallant.github.io.todoapp.OffsetHolder;
import degallant.github.io.todoapp.common.LinkBuilder;
import degallant.github.io.todoapp.domain.users.UserEntity;
import degallant.github.io.todoapp.sanitization.FieldValidator;
import degallant.github.io.todoapp.sanitization.Sanitizer;
import degallant.github.io.todoapp.sanitization.parsers.TagsFieldParser;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.mediatype.hal.HalModelBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
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
    private final LinkBuilder link;
    private final TagsFieldParser tagsParser;
    private final OffsetHolder offsetHolder;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody TagsDto.Tag request, Authentication authentication) {

        var user = (UserEntity) authentication.getPrincipal();

        var result = sanitizer.sanitize(
                sanitizer.field("name").withRequiredValue(request.getName()).sanitize(value -> {
                    rules.isNotEmpty(value);
                    return value;
                })
        );

        var entity = TagEntity.builder()
                .name(result.get("name").value())
                .user(user)
                .build();

        entity = repository.save(entity);

        var linkCreated = link.to("tags").slash(entity.getId()).withSelfRel();

        return ResponseEntity.created(linkCreated.toUri()).build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> patch(@PathVariable String id, @RequestBody TagsDto.Tag request, Authentication authentication) {

        var user = (UserEntity) authentication.getPrincipal();
        var tag = tagsParser.toTagOrThrowNoSuchElement(id, user);

        var result = sanitizer.sanitize(
                sanitizer.field("name").withOptionalValue(request.getName()).sanitize(value -> {
                    rules.isNotEmpty(value);
                    return value;
                })
        );

        if (!result.hasAnyFieldWithValue()) {
            return ResponseEntity.noContent().build();
        }

        result.get("name").consumeIfExists(tag::setName);
        repository.save(tag);

        return ResponseEntity.ok().build();

    }

    @GetMapping("/{id}")
    public ResponseEntity<?> details(@PathVariable String id, Authentication authentication) {

        var user = (UserEntity) authentication.getPrincipal();
        var entity = tagsParser.toTagOrThrowNoSuchElement(id, user);
        var response = toEntityModel(entity);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable String id, Authentication authentication) {

        var user = (UserEntity) authentication.getPrincipal();
        var tag = tagsParser.toTagOrThrowNoSuchElement(id, user);

        tag.setDeletedAt(OffsetDateTime.now());
        repository.save(tag);

        return ResponseEntity.noContent().build();

    }

    @GetMapping
    public ResponseEntity<?> list(Authentication authentication) {

        var user = (UserEntity) authentication.getPrincipal();

        var tags = repository.findByUserIdAndDeletedAtIsNull(user.getId())
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
                .createdAt(offsetHolder.applyTo(entity.getCreatedAt()))
                .updatedAt(offsetHolder.applyTo(entity.getUpdatedAt()))
                .build();

        return EntityModel.of(tag).add(linkSelf, linkAll);
    }

}
