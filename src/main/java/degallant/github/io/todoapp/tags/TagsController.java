package degallant.github.io.todoapp.tags;

import com.google.api.client.http.HttpStatusCodes;
import degallant.github.io.todoapp.users.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.mediatype.hal.HalModelBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/tags")
public class TagsController {

    private final TagsRepository repository;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody TagsDto.Create request, Authentication authentication) {

        var tagEntity = TagEntity.builder()
                .name(request.getName())
                .userId(((UserEntity) authentication.getPrincipal()).getId())
                .build();

        tagEntity = repository.save(tagEntity);

        return ResponseEntity
                .status(HttpStatusCodes.STATUS_CODE_CREATED)
                .body(TagsDto.Details.builder().id(tagEntity.getId()).name(tagEntity.getName()).build());

    }

    @GetMapping
    public RepresentationModel<?> list(Authentication authentication) {
        UUID userId = ((UserEntity) authentication.getPrincipal()).getId();

        List<TagsDto.Details> tags = repository.findByUserId(userId).stream()
                .map(tag -> TagsDto.Details.builder().name(tag.getName()).id(tag.getId()).build())
                .collect(Collectors.toList());

        var selfRef = linkTo(methodOn(getClass()).list(authentication)).withSelfRel();

        if (tags.isEmpty()) {
            return HalModelBuilder.emptyHalModel()
                    .embed(Collections.emptyList(), TagsDto.Details.class)
                    .link(selfRef).build();
        }

        return CollectionModel.of(tags).add(selfRef);
    }

}
