package degallant.github.io.todoapp.authentication;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.IncorrectClaimException;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import degallant.github.io.todoapp.users.UserEntity;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Component
public class JwtToken {

    private final AuthenticationConfiguration config;

    private final Algorithm signature;

    public JwtToken(AuthenticationConfiguration config) {
        this.config = config;
        this.signature = Algorithm.HMAC256(config.signKey());
    }

    public String makeAccessTokenFor(UserEntity entity) {
        return makeAccessTokenFor(entity, Instant.now().plus(config.accessExpiryMinutes(), ChronoUnit.MINUTES));
    }

    public String makeRefreshToken(UserEntity entity) {
        return makeRefreshToken(entity.getId(), Instant.now().plus(config.refreshExpiryMinutes(), ChronoUnit.MINUTES));
    }

    public String makeAccessTokenFor(UserEntity entity, Instant expiresAt) {
        return makeAccessTokenFor(entity.getId(), expiresAt);
    }

    public String makeAccessTokenFor(UUID userId, Instant expiresAt) {
        return JWT.create()
                .withIssuer(config.issuer())
                .withSubject(userId.toString())
                .withExpiresAt(expiresAt)
                .sign(signature);
    }

    public String makeAccessTokenWithIssuer(UUID userId, String issuer) {
        return JWT.create()
                .withIssuer(issuer)
                .withSubject(userId.toString())
                .withExpiresAt(Instant.now().plus(config.accessExpiryMinutes(), ChronoUnit.MINUTES))
                .sign(signature);
    }

    public String makeRefreshToken(UUID userId, Instant expiresAt) {
        return JWT.create()
                .withIssuer(config.issuer())
                .withSubject(userId.toString())
                .withExpiresAt(expiresAt)
                .sign(signature);
    }

    public UUID parseToUserId(String token) throws JwtTokenException {
        try {
            JWTVerifier verifier = JWT.require(signature)
                    .withIssuer(config.issuer())
                    .build();

            DecodedJWT decodedJWT = verifier.verify(token);

            return UUID.fromString(decodedJWT.getSubject());
        } catch (TokenExpiredException exception) {
            throw new JwtTokenException.Expired(exception);
        } catch (JWTDecodeException exception) {
            throw new JwtTokenException.Invalid(exception);
        } catch (IllegalArgumentException exception) {
            throw new JwtTokenException.InvalidSubject(exception);
        } catch (IncorrectClaimException exception) {
            throw new JwtTokenException.InvalidClaim(exception);
        }
    }

}
