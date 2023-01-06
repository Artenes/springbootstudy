package degallant.github.io.todoapp.authentication;

import degallant.github.io.todoapp.user.UserController;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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

        Authentication authenticatedUser = service.authenticateWithOpenId(request.getOpenIdToken());
        AuthenticationService.TokenPair tokenPair = (AuthenticationService.TokenPair) authenticatedUser.getCredentials();
        boolean isNew = (Boolean) authenticatedUser.getDetails();

        Link link = linkTo(UserController.class).withSelfRel();
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

}
