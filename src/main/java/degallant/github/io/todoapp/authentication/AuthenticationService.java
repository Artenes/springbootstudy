package degallant.github.io.todoapp.authentication;

import degallant.github.io.todoapp.openid.OpenIdExtractionException;
import degallant.github.io.todoapp.openid.OpenIdTokenParser;
import degallant.github.io.todoapp.openid.OpenIdUser;
import degallant.github.io.todoapp.users.Role;
import degallant.github.io.todoapp.users.UserEntity;
import degallant.github.io.todoapp.users.UsersRepository;
import degallant.github.io.todoapp.validation.InvalidValueException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

/**
 * @noinspection ClassCanBeRecord
 */
@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UsersRepository repository;

    private final OpenIdTokenParser openIdTokenParser;

    private final JwtToken token;

    public OpenIdUser parseOpenIdOrThrow(String openIdToken) throws InvalidValueException {
        try {
            return openIdTokenParser.extract(openIdToken);
        } catch (OpenIdExtractionException exception) {
            var message = exception instanceof OpenIdExtractionException.FailedParsing ? "validation.openid_extraction_failed" : "validation.openid_invalid_token";
            throw new InvalidValueException(exception, message, exception.getToken());
        }
    }

    public TokenPair refresh(UserEntity user) {
        String accessToken = token.makeAccessTokenFor(user);
        String refreshToken = token.makeRefreshToken(user);
        return new TokenPair(accessToken, refreshToken);
    }

    public Authentication authenticateWith(OpenIdUser openIdUser) {
        Optional<UserEntity> user = repository.findByEmail(openIdUser.email());

        UserEntity userEntity;
        boolean isNewUser;

        if (user.isPresent()) {
            userEntity = user.get();
            isNewUser = false;
        } else {
            UserEntity newUser = UserEntity.builder()
                    .email(openIdUser.email())
                    .name(openIdUser.name())
                    .pictureUrl(openIdUser.pictureUrl())
                    .role(Role.ROLE_USER)
                    .build();
            userEntity = repository.save(newUser);
            isNewUser = true;
        }

        var tokens = refresh(userEntity);

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userEntity,
                tokens,
                Collections.emptyList()
        );
        authentication.setDetails(isNewUser);
        return authentication;
    }

    public Authentication authenticateWithJwtToken(String jwtToken) throws JwtTokenException {
        var userId = token.parseToUserId(jwtToken);
        var user = repository.findById(userId);

        if (user.isEmpty()) {
            throw new JwtTokenException.InvalidSubject(jwtToken);
        }

        return new UsernamePasswordAuthenticationToken(user.get(), null, user.get().roles());
    }

    public record TokenPair(String accessToken, String refreshToken) {

    }

}
