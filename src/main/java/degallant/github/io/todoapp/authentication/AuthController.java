package degallant.github.io.todoapp.authentication;

import degallant.github.io.todoapp.projects.ProjectsController;
import degallant.github.io.todoapp.tags.TagsController;
import degallant.github.io.todoapp.tasks.TasksController;
import degallant.github.io.todoapp.users.UserEntity;
import degallant.github.io.todoapp.users.UsersDto;
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
    public ResponseEntity<?> authenticate(@RequestBody AuthDto.Authenticate request) {

        Authentication authenticatedUser = service.authenticateWithOpenId(request.getOpenIdToken());
        AuthenticationService.TokenPair tokenPair = (AuthenticationService.TokenPair) authenticatedUser.getCredentials();
        boolean isNew = (Boolean) authenticatedUser.getDetails();

        Link link = linkTo(methodOn(getClass()).profile(authenticatedUser)).withSelfRel();
        EntityModel<AuthDto.TokenPair> model = EntityModel.of(AuthDto.TokenPair.builder()
                .accessToken(tokenPair.accessToken())
                .refreshToken(tokenPair.refreshToken())
                .build()
        ).add(link);

        if (isNew) {
            return ResponseEntity.created(link.toUri()).body(model);
        }

        return ResponseEntity.ok().body(model);

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
        Link tasksLink = linkTo(methodOn(TasksController.class).list(authentication)).withRel("tasks");
        Link tagsLink = linkTo(methodOn(TagsController.class).list(authentication)).withRel("tags");
        Link projectsLink = linkTo(methodOn(ProjectsController.class).list(authentication)).withRel("projects");

        var response = EntityModel.of(user).add(selfLink, tasksLink, tagsLink, projectsLink);

        return ResponseEntity.ok(response);
    }

}
