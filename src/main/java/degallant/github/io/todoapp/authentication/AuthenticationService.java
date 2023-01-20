package degallant.github.io.todoapp.authentication;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import degallant.github.io.todoapp.openid.OpenIdTokenParser;
import degallant.github.io.todoapp.openid.OpenIdUser;
import degallant.github.io.todoapp.users.UserEntity;
import degallant.github.io.todoapp.users.UsersRepository;
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

    private final UsersRepository repository;

    private final OpenIdTokenParser openIdTokenParser;

    private final AuthenticationConfiguration config;

    private final Algorithm signature;

    public AuthenticationService(UsersRepository repository, OpenIdTokenParser openIdTokenParser, AuthenticationConfiguration config) {
        this.repository = repository;
        this.openIdTokenParser = openIdTokenParser;
        this.config = config;
        signature = Algorithm.HMAC256(config.signKey());
    }

    public Authentication authenticateWithOpenId(String openIdToken) {
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

        String accessToken = JWT.create()
                .withIssuer(config.issuer())
                .withSubject(userEntity.getId().toString())
                .withExpiresAt(Instant.now().plus(config.accessExpiryMinutes(), ChronoUnit.MINUTES))
                .sign(signature);

        String refreshToken = JWT.create()
                .withIssuer(config.issuer())
                .withExpiresAt(Instant.now().plus(config.refreshExpiryMinutes(), ChronoUnit.MINUTES))
                .sign(signature);

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userEntity,
                new TokenPair(accessToken, refreshToken),
                Collections.emptyList()
        );
        authentication.setDetails(isNewUser);
        return authentication;
    }

    public Authentication authenticateWithJwtToken(String jwtToken) {
        JWTVerifier verifier = JWT.require(signature)
                .withIssuer(config.issuer())
                .build();

        DecodedJWT decodedJWT = verifier.verify(jwtToken);

        UUID userId = UUID.fromString(decodedJWT.getSubject());

        UserEntity user = repository.findById(userId).orElseThrow();

        return new UsernamePasswordAuthenticationToken(user, null, Collections.emptyList());
    }

    public record TokenPair(String accessToken, String refreshToken) {

    }

}
