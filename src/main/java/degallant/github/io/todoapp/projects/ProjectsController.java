package degallant.github.io.todoapp.projects;

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

import java.util.UUID;
import java.util.stream.Collectors;

import static degallant.github.io.todoapp.common.LinkBuilder.makeLinkTo;

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
    private final FieldParser parser;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody ProjectsDto.Create request, Authentication authentication) {

        var userId = ((UserEntity) authentication.getPrincipal()).getId();

        var result = sanitizer.sanitize(
                sanitizer.field("title").withRequiredValue(request.getTitle()).sanitize(value -> {
                    rules.isNotEmpty(value);
                    return value;
                })
        );

        var entity = ProjectEntity.builder()
                .title(result.get("title").value())
                .userId(userId)
                .build();

        entity = repository.save(entity);

        var link = makeLinkTo("v1", "projects", entity.getId()).withSelfRel();

        return ResponseEntity.created(link.toUri()).build();
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

        var projects = repository.findByUserId(userId)
                .stream()
                .map(this::toEntityModel)
                .collect(Collectors.toList());

        var linkSelf = makeLinkTo("v1", "projects").withSelfRel();

        var response = HalModelBuilder.emptyHalModel()
                .embed(projects, ProjectsDto.Details.class)
                .link(linkSelf).build();

        return ResponseEntity.ok(response);
    }

    private EntityModel<ProjectsDto.Details> toEntityModel(ProjectEntity entity) {
        var project = ProjectsDto.Details.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .build();
        var linkSelf = makeLinkTo("v1", "projects", entity.getId()).withSelfRel();
        var linkAll = makeLinkTo("v1", "projects").withRel("all");
        return EntityModel.of(project).add(linkSelf, linkAll);
    }

}
