package degallant.github.io.todoapp.projects;

import degallant.github.io.todoapp.users.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/projects")
public class ProjectsController {

    private final ProjectRepository repository;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody ProjectDto.Create request, Authentication authentication) {

        var entity = ProjectEntity.builder()
                .title(request.getTitle())
                .userId(((UserEntity) authentication.getPrincipal()).getId())
                .build();

        entity = repository.save(entity);

        var link = WebMvcLinkBuilder.linkTo(ProjectsController.class).slash(entity.getId());

        return ResponseEntity.created(link.toUri()).build();

    }

    @GetMapping("/{id}")
    public ProjectDto.Details get(@PathVariable UUID id, Authentication authentication) {

        var entity = repository.findByIdAndUserId(id, ((UserEntity) authentication.getPrincipal()).getId()).orElseThrow();

        return ProjectDto.Details.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .build();

    }

    @GetMapping
    public List<ProjectDto.Details> list(Authentication authentication) {
        return Collections.emptyList();
    }

}
