package degallant.github.io.todoapp.authentication;

import degallant.github.io.todoapp.common.LinkBuilder;
import degallant.github.io.todoapp.domain.projects.ProjectsController;
import degallant.github.io.todoapp.domain.tags.TagsController;
import degallant.github.io.todoapp.domain.tasks.TasksController;
import degallant.github.io.todoapp.domain.users.UserEntity;
import degallant.github.io.todoapp.domain.users.UsersDto;
import degallant.github.io.todoapp.domain.users.UsersRepository;
import degallant.github.io.todoapp.exceptions.AppExceptionHandler;
import degallant.github.io.todoapp.exceptions.InvalidStateException;
import degallant.github.io.todoapp.sanitization.FieldAndErrorMessage;
import degallant.github.io.todoapp.sanitization.FieldValidator;
import degallant.github.io.todoapp.sanitization.InvalidRequestException;
import degallant.github.io.todoapp.sanitization.Sanitizer;
import degallant.github.io.todoapp.sanitization.parsers.PrimitiveFieldParser;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * @noinspection ClassCanBeRecord, unused
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/auth")
public class AuthController {

    private final AuthenticationService service;
    private final Sanitizer sanitizer;
    private final FieldValidator rules;
    private final PrimitiveFieldParser parser;
    private final LinkBuilder link;
    private final AppExceptionHandler handler;
    private final UsersRepository usersRepository;

    @PostMapping("email")
    public ResponseEntity<?> authenticateWithEmail(@RequestBody AuthDto.Authenticate request) {

        var result = sanitizer.sanitize(
                sanitizer.field("email").withRequiredValue(request.getEmail()).sanitize(value -> {
                    rules.isEmail(value);
                    return value;
                }),
                sanitizer.field("password").withRequiredValue(request.getPassword()).sanitize(value -> {
                    rules.isNotEmpty(value);
                    return value;
                })
        );

        try {
            var authenticatedUser = service.authenticateWithEmail(result.get("email").value(), result.get("password").value());
            var credentials = (AuthenticationService.TokenPair) authenticatedUser.getCredentials();

            var tokenPair = AuthDto.TokenPair.builder()
                    .accessToken(credentials.accessToken())
                    .refreshToken(credentials.refreshToken())
                    .build();

            var linkSelf = link.to("auth").slash("profile").withSelfRel();
            var response = EntityModel.of(tokenPair).add(linkSelf);

            return ResponseEntity.ok().body(response);

        } catch (InvalidStateException exception) {
            switch (exception.getMessageId()) {
                case "error.email_not_found" -> throw new InvalidRequestException(new FieldAndErrorMessage("email", "body", "error.email_not_found", result.get("email")));
                case "error.invalid_password" -> throw new InvalidRequestException(new FieldAndErrorMessage("password", "body", "error.invalid_password"));
            }
            throw exception;
        }

    }

    @PostMapping
    public ResponseEntity<?> authenticate(@RequestBody AuthDto.Authenticate request) {

        var result = sanitizer.sanitize(
                sanitizer.field("open_id_token").withRequiredValue(request.getOpenIdToken()).sanitize(value -> {
                    rules.isNotEmpty(value);
                    return service.parseOpenIdOrThrow(value);
                })
        );

        var authenticatedUser = service.authenticateWith(result.get("open_id_token").value());
        var credentials = (AuthenticationService.TokenPair) authenticatedUser.getCredentials();
        var isNew = (Boolean) authenticatedUser.getDetails();

        var tokenPair = AuthDto.TokenPair.builder()
                .accessToken(credentials.accessToken())
                .refreshToken(credentials.refreshToken())
                .build();

        var linkSelf = link.to("auth").slash("profile").withSelfRel();
        var response = EntityModel.of(tokenPair).add(linkSelf);

        if (isNew) {
            return ResponseEntity.created(linkSelf.toUri()).body(response);
        }

        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/refresh")
    public ResponseEntity<?> refresh(Authentication authentication) {

        var user = (UserEntity) authentication.getPrincipal();
        var tokens = service.refresh(user);

        var tokenPair = AuthDto.TokenPair.builder()
                .accessToken(tokens.accessToken())
                .refreshToken(tokens.refreshToken())
                .build();

        return ResponseEntity.ok(tokenPair);
    }

    @GetMapping("/profile")
    public ResponseEntity<?> profile(Authentication authentication) {
        var entity = (UserEntity) authentication.getPrincipal();
        var user = UsersDto.Details.builder()
                .id(entity.getId().toString())
                .name(entity.getName())
                .email(entity.getEmail())
                .pictureUrl(entity.getPictureUrl())
                .role(entity.getRole().name())
                .build();

        Link selfLink = linkTo(methodOn(getClass()).profile(authentication)).withSelfRel();
        Link tasksLink = linkTo(methodOn(TasksController.class).list(null, null, null, null, null, authentication)).withRel("tasks");
        Link tagsLink = linkTo(methodOn(TagsController.class).list(authentication)).withRel("tags");
        Link projectsLink = linkTo(methodOn(ProjectsController.class).list(authentication)).withRel("projects");

        var response = EntityModel.of(user).add(selfLink, tasksLink, tagsLink, projectsLink);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/profile")
    public ResponseEntity<?> patch(@RequestBody AuthDto.Patch request, Authentication authentication) {
        var user = (UserEntity) authentication.getPrincipal();

        var result = sanitizer.sanitize(
                sanitizer.field("name").withOptionalValue(request.getName()).sanitize(value -> {
                    rules.isNotEmpty(value);
                    return value;
                }),

                sanitizer.field("picture_url").withOptionalValue(request.getPictureUrl()).sanitize(value -> {
                    rules.isNotEmpty(value);
                    rules.isURL(value);
                    return value;
                })
        );

        user.setName(result.get("name").ifNull(user.getName()));
        user.setPictureUrl(result.get("picture_url").ifNull(user.getPictureUrl()));

        usersRepository.save(user);

        return ResponseEntity.ok().build();
    }

}
