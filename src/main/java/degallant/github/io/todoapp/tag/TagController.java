package degallant.github.io.todoapp.tag;

import degallant.github.io.todoapp.user.UserEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URISyntaxException;
import java.util.UUID;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@RestController
@RequestMapping("/v1/tag")
public class TagController {

    private final TagRepository repository;

    public TagController(TagRepository repository) {
        this.repository = repository;
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody TagDto.Create request, Authentication authentication) throws URISyntaxException {

        var tagEntity = TagEntity.builder()
                .name(request.getName())
                .userId(((UserEntity) authentication.getPrincipal()).getId())
                .build();

        tagEntity = repository.save(tagEntity);

        var link = linkTo(TagController.class).slash(tagEntity.getId());

        return ResponseEntity.created(link.toUri()).build();

    }

    @GetMapping("/{id}")
    public TagDto.Details get(@PathVariable UUID id, Authentication authentication) {

        var tag = repository.findByIdAndUserId(id, ((UserEntity) authentication.getPrincipal()).getId()).orElseThrow();

        return TagDto.Details.builder()
                .name(tag.getName())
                .build();

    }

}
