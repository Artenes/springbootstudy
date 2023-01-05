package degallant.github.io.todoapp;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthenticationService {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationService.class);

    private final UserRepository repository;

    private final OpenIdTokenParser openIdTokenParser;

    private final AuthenticationConfiguration config;

    private final Algorithm signature;

    public AuthenticationService(UserRepository repository, OpenIdTokenParser openIdTokenParser, AuthenticationConfiguration config) {
        this.repository = repository;
        this.openIdTokenParser = openIdTokenParser;
        this.config = config;
        signature = Algorithm.HMAC256(config.signKey());
    }

    public AuthenticatedUser authenticateWithOpenId(String openIdToken) {

        OpenIdUser openIdUser = openIdTokenParser.extract(openIdToken);

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

    public Authentication authenticateWithJWTToken(String jwtToken) {

        JWTVerifier verifier = JWT.require(signature)
                .withIssuer(config.issuer())
                .build();

        DecodedJWT decodedJWT = verifier.verify(jwtToken);

        UUID userId = UUID.fromString(decodedJWT.getSubject());

        UserEntity user = repository.findById(userId).orElseThrow();

        log.info("User found: " + user.getId());

        return new UsernamePasswordAuthenticationToken(user, null, Collections.emptyList());

    }

    public TokenPair encode(UserEntity user) {

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
