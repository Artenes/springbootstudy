package degallant.github.io.todoapp.authentication;

import degallant.github.io.todoapp.domain.users.Role;
import degallant.github.io.todoapp.domain.users.UserEntity;
import degallant.github.io.todoapp.domain.users.UsersRepository;
import degallant.github.io.todoapp.exceptions.InvalidStateException;
import degallant.github.io.todoapp.openid.OpenIdExtractionException;
import degallant.github.io.todoapp.openid.OpenIdTokenParser;
import degallant.github.io.todoapp.openid.OpenIdUser;
import degallant.github.io.todoapp.sanitization.InvalidValueException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

/**
 * @noinspection ClassCanBeRecord
 */
@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UsersRepository repository;
    private final OpenIdTokenParser openIdTokenParser;
    private final JwtToken token;
    private final PasswordEncoder passwordEncoder;
    private final ApiKeyRepository apiKeyRepository;

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

    public Authentication authenticateAdminWith(OpenIdUser openIdUser) throws InvalidStateException {

        Optional<UserEntity> user = repository.findByEmail(openIdUser.email());
        Optional<UserEntity> anAdmin = repository.findByRole(Role.ROLE_ADMIN);

        if (user.isEmpty() && anAdmin.isPresent()) {
            throw new InvalidStateException("error.user_cannot_create_another_admin", openIdUser.email());
        }

        if (user.isPresent() && user.get().isDeleted()) {
            throw new InvalidStateException("error.user_deleted", user.get().getId());
        }

        if (user.isPresent() && user.get().getRole() != Role.ROLE_ADMIN) {
            throw new InvalidStateException("error.user_invalid_role", user.get().getId(), user.get().getRole());
        }

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
                    .role(Role.ROLE_ADMIN)
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

    public Authentication authenticateWith(OpenIdUser openIdUser) throws InvalidStateException {
        Optional<UserEntity> user = repository.findByEmail(openIdUser.email());

        UserEntity userEntity;
        boolean isNewUser;

        if (user.isPresent() && user.get().isDeleted()) {
            throw new InvalidStateException("error.user_deleted", user.get().getId());
        }

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

    public Authentication authenticateWithEmail(String email, String password) throws InvalidStateException {
        Optional<UserEntity> user = repository.findByEmail(email);

        if (user.isPresent() && user.get().isDeleted()) {
            throw new InvalidStateException("error.user_deleted", user.get().getId());
        }

        if (user.isEmpty()) {
            throw new InvalidStateException("error.email_not_found", email);
        }

        if (!passwordEncoder.matches(password, user.get().getPassword())) {
            throw new InvalidStateException("error.invalid_password");
        }

        var tokens = refresh(user.get());

        return new UsernamePasswordAuthenticationToken(
                user.get(),
                tokens,
                Collections.emptyList()
        );

    }

    public Authentication authenticateWithJwtToken(String jwtToken) throws JwtTokenException {
        var userId = token.parseToUserId(jwtToken);
        var user = repository.findByIdAndDeletedAtIsNull(userId);

        if (user.isEmpty()) {
            throw new JwtTokenException.InvalidSubject(jwtToken);
        }

        return new UsernamePasswordAuthenticationToken(user.get(), null, user.get().roles());
    }

    public Authentication authenticateWithKeys(String rawApiId, String apiSecret) throws InvalidStateException {

        UUID apiKeyId;

        try {
            apiKeyId = UUID.fromString(rawApiId);
        } catch (IllegalArgumentException exception) {
            throw new InvalidStateException("error.api_key_invalid", rawApiId);
        }

        var keyEntity = apiKeyRepository.findById(apiKeyId);

        if (keyEntity.isEmpty() || keyEntity.get().isDeleted()) {
            throw new InvalidStateException("error.api_key_not_found", apiKeyId);
        }

        if (!passwordEncoder.matches(apiSecret, keyEntity.get().getSecret())) {
            throw new InvalidStateException("error.api_key_invalid_secret", apiKeyId);
        }

        return new UsernamePasswordAuthenticationToken(keyEntity.get(), null, Collections.emptyList());

    }

    public record TokenPair(String accessToken, String refreshToken) {

    }

}
