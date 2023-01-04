package degallant.github.io.todoapp;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@RestController
@RequestMapping("/v1/auth")
public class AuthenticationController {

    private final AuthenticationService service;

    public AuthenticationController(AuthenticationService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<?> authenticate(@RequestBody AuthDto.Authenticate request) {

        AuthenticatedUser authenticatedUser = service.authenticate(request.getToken());

        Link link = linkTo(UserController.class).slash(authenticatedUser.user().getId()).withSelfRel();
        EntityModel<AuthDto.TokenPair> model = EntityModel.of(AuthDto.TokenPair.builder()
                .accessToken(authenticatedUser.accessToken())
                .refreshToken(authenticatedUser.refreshToken())
                .build()
        ).add(link);

        if (authenticatedUser.isNew()) {
            return ResponseEntity.created(link.toUri()).body(model);
        }

        return ResponseEntity.ok().body(model);

    }

}
