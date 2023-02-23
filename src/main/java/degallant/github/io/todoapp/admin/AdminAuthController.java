package degallant.github.io.todoapp.admin;

import degallant.github.io.todoapp.authentication.AuthDto;
import degallant.github.io.todoapp.authentication.AuthenticationService;
import degallant.github.io.todoapp.common.LinkBuilder;
import degallant.github.io.todoapp.domain.users.UsersRepository;
import degallant.github.io.todoapp.exceptions.AppExceptionHandler;
import degallant.github.io.todoapp.sanitization.FieldValidator;
import degallant.github.io.todoapp.sanitization.Sanitizer;
import degallant.github.io.todoapp.sanitization.parsers.PrimitiveFieldParser;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @noinspection ClassCanBeRecord, unused
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/admin")
public class AdminAuthController {

    private final AuthenticationService service;
    private final Sanitizer sanitizer;
    private final FieldValidator rules;
    private final PrimitiveFieldParser parser;
    private final LinkBuilder link;
    private final AppExceptionHandler handler;
    private final UsersRepository usersRepository;

    @PostMapping("/auth")
    public ResponseEntity<?> authenticate(@RequestBody AuthDto.Authenticate request) {

        var result = sanitizer.sanitize(
                sanitizer.field("open_id_token").withRequiredValue(request.getOpenIdToken()).sanitize(value -> {
                    rules.isNotEmpty(value);
                    return service.parseOpenIdOrThrow(value);
                }).withName("open_id_user")
        );

        var authenticatedUser = service.authenticateAdminWith(result.get("open_id_user").value());
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

}
