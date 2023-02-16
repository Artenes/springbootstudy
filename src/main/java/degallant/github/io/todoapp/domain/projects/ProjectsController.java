package degallant.github.io.todoapp.domain.projects;

import degallant.github.io.todoapp.OffsetHolder;
import degallant.github.io.todoapp.common.LinkBuilder;
import degallant.github.io.todoapp.domain.users.UserEntity;
import degallant.github.io.todoapp.sanitization.FieldValidator;
import degallant.github.io.todoapp.sanitization.Sanitizer;
import degallant.github.io.todoapp.sanitization.parsers.ProjectsFieldParser;
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
@RequestMapping("/v1/projects")
public class ProjectsController {

    private final ProjectsRepository repository;
    private final Sanitizer sanitizer;
    private final FieldValidator rules;
    private final LinkBuilder link;
    private final ProjectsFieldParser projectsParser;
    private final OffsetHolder offsetHolder;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody ProjectsDto.Project request, Authentication authentication) {

        var user = (UserEntity) authentication.getPrincipal();

        var result = sanitizer.sanitize(
                sanitizer.field("title").withRequiredValue(request.getTitle()).sanitize(value -> {
                    rules.isNotEmpty(value);
                    return value;
                })
        );

        var entity = ProjectEntity.builder()
                .title(result.get("title").value())
                .user(user)
                .build();

        entity = repository.save(entity);

        var linkSelf = link.to("projects").slash(entity.getId()).withSelfRel();

        return ResponseEntity.created(linkSelf.toUri()).build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> details(@PathVariable String id, Authentication authentication) {

        var user = (UserEntity) authentication.getPrincipal();
        var entity = projectsParser.toProjectOrThrowNoSuchElement(id, user);

        var response = toEntityModel(entity);

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<?> list(Authentication authentication) {

        var user = (UserEntity) authentication.getPrincipal();

        var projects = repository.findByUserIdAndDeletedAtIsNull(user.getId())
                .stream()
                .map(this::toEntityModel)
                .collect(Collectors.toList());

        var linkSelf = link.to("projects").withSelfRel();

        var response = HalModelBuilder.emptyHalModel()
                .embed(projects, ProjectsDto.Details.class)
                .link(linkSelf).build();

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> patch(@PathVariable String id, @RequestBody ProjectsDto.Project request, Authentication authentication) {

        var user = (UserEntity) authentication.getPrincipal();
        var project = projectsParser.toProjectOrThrowNoSuchElement(id, user);

        var result = sanitizer.sanitize(sanitizer.field("title").withOptionalValue(request.getTitle()).sanitize(value -> {
            rules.isNotEmpty(value);
            return value;
        }));

        if (!result.hasAnyFieldWithValue()) {
            return ResponseEntity.noContent().build();
        }

        result.get("title").consumeIfExists(project::setTitle);
        repository.save(project);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable String id, Authentication authentication) {

        var user = (UserEntity) authentication.getPrincipal();
        var project = projectsParser.toProjectOrThrowNoSuchElement(id, user);

        project.setDeletedAt(OffsetDateTime.now());
        repository.save(project);

        return ResponseEntity.noContent().build();

    }

    private EntityModel<ProjectsDto.Details> toEntityModel(ProjectEntity entity) {
        var project = ProjectsDto.Details.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .createdAt(offsetHolder.applyTo(entity.getCreatedAt()))
                .updatedAt(offsetHolder.applyTo(entity.getUpdatedAt()))
                .build();
        var linkSelf = link.to("projects").slash(entity.getId()).withSelfRel();
        var linkAll = link.to("projects").withRel("all");
        return EntityModel.of(project).add(linkSelf, linkAll);
    }

}
