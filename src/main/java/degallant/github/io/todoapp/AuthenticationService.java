package degallant.github.io.todoapp;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Service
public class AuthenticationService {

    private final UserRepository repository;

    private final OpenIdTokenParser openIdTokenParser;

    private final AuthenticationConfiguration config;

    public AuthenticationService(UserRepository repository, OpenIdTokenParser openIdTokenParser, AuthenticationConfiguration config) {
        this.repository = repository;
        this.openIdTokenParser = openIdTokenParser;
        this.config = config;
    }

    public AuthenticatedUser authenticate(String token) {

        OpenIdUser openIdUser = openIdTokenParser.extract(token);

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
                    .build();
            userEntity = repository.save(newUser);
            isNewUser = true;
        }

        TokenPair pair = encode(userEntity);

        return new AuthenticatedUser(userEntity, isNewUser, pair.accessToken(), pair.refreshToken());

    }

    public TokenPair encode(UserEntity user) {

        Algorithm signature = Algorithm.HMAC256(config.signKey());

        String token = JWT.create()
                .withIssuer(config.issuer())
                .withSubject(user.getId().toString())
                .withExpiresAt(Instant.now().plus(config.accessExpiryMinutes(), ChronoUnit.MINUTES))
                .sign(signature);

        String refresh = JWT.create()
                .withIssuer(config.issuer())
                .withExpiresAt(Instant.now().plus(config.refreshExpiryMinutes(), ChronoUnit.MINUTES))
                .sign(signature);

        return new TokenPair(token, refresh);

    }

}
