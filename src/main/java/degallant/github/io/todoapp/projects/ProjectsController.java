package degallant.github.io.todoapp.projects;

import degallant.github.io.todoapp.users.UserEntity;
import jakarta.validation.Valid;
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

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/projects")
public class ProjectsController {

    private final ProjectsRepository repository;

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody ProjectsDto.Create request, Authentication authentication) {
        var entity = ProjectEntity.builder()
                .title(request.getTitle())
                .userId(((UserEntity) authentication.getPrincipal()).getId())
                .build();

        entity = repository.save(entity);

        var link = linkTo(methodOn(getClass()).details(entity.getId(), authentication)).withSelfRel();

        return ResponseEntity.created(link.toUri()).build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> details(@PathVariable UUID id, Authentication authentication) {
        var userId = ((UserEntity) authentication.getPrincipal()).getId();
        var entity = repository.findByIdAndUserId(id, userId).orElseThrow();

        EntityModel<ProjectsDto.Details> response = toEntityModel(entity, authentication);

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public RepresentationModel<?> list(Authentication authentication) {
        UUID userId = ((UserEntity) authentication.getPrincipal()).getId();

        var projects = repository.findByUserId(userId)
                .stream()
                .map(entity -> toEntityModel(entity, authentication))
                .collect(Collectors.toList());

        var linkSelf = linkTo(methodOn(getClass()).list(authentication)).withSelfRel();

        if (projects.isEmpty()) {
            return HalModelBuilder.emptyHalModel()
                    .embed(Collections.emptyList(), ProjectsDto.Details.class)
                    .link(linkSelf).build();
        }

        return CollectionModel.of(projects).add(linkSelf);
    }

    private EntityModel<ProjectsDto.Details> toEntityModel(ProjectEntity entity, Authentication authentication) {
        var project = ProjectsDto.Details.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .build();

        var linkSelf = linkTo(methodOn(getClass()).details(entity.getId(), authentication)).withSelfRel();
        var linkAll = linkTo(methodOn(getClass()).list(authentication)).withRel("all");

        return EntityModel.of(project).add(linkSelf, linkAll);
    }

}
