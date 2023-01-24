package degallant.github.io.todoapp.authentication;

import degallant.github.io.todoapp.projects.ProjectsController;
import degallant.github.io.todoapp.tags.TagsController;
import degallant.github.io.todoapp.tasks.TasksController;
import degallant.github.io.todoapp.users.UserEntity;
import degallant.github.io.todoapp.users.UsersDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/auth")
public class AuthController {

    private final AuthenticationService service;

    @PostMapping
    public ResponseEntity<?> authenticate(@Valid @RequestBody AuthDto.Authenticate request) {
        var authenticatedUser = service.authenticateWithOpenId(request.getOpenIdToken());
        var credentials = (AuthenticationService.TokenPair) authenticatedUser.getCredentials();
        var isNew = (Boolean) authenticatedUser.getDetails();

        var tokenPair = AuthDto.TokenPair.builder()
                .accessToken(credentials.accessToken())
                .refreshToken(credentials.refreshToken())
                .build();
        var linkSelf = linkTo(methodOn(getClass()).profile(authenticatedUser)).withSelfRel();
        var response = EntityModel.of(tokenPair).add(linkSelf);

        if (isNew) {
            return ResponseEntity.created(linkSelf.toUri()).body(response);
        }

        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/profile")
    public ResponseEntity<?> profile(Authentication authentication) {
        var entity = (UserEntity) authentication.getPrincipal();
        var user = UsersDto.Details.builder()
                .name(entity.getName())
                .email(entity.getEmail())
                .pictureUrl(entity.getPictureUrl())
                .build();

        Link selfLink = linkTo(methodOn(getClass()).profile(authentication)).withSelfRel();
        Link tasksLink = linkTo(methodOn(TasksController.class).list(0, authentication)).withRel("tasks");
        Link tagsLink = linkTo(methodOn(TagsController.class).list(authentication)).withRel("tags");
        Link projectsLink = linkTo(methodOn(ProjectsController.class).list(authentication)).withRel("projects");

        var response = EntityModel.of(user).add(selfLink, tasksLink, tagsLink, projectsLink);

        return ResponseEntity.ok(response);
    }

}
