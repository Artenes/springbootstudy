package degallant.github.io.todoapp.authentication;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.IncorrectClaimException;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import degallant.github.io.todoapp.users.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class JwtToken {

    private final AuthenticationConfiguration config;
    private final Algorithm signature;

    public JwtToken(AuthenticationConfiguration config) {
        this.config = config;
        this.signature = Algorithm.HMAC256(config.signKey());
    }

    public Builder make() {
        return new Builder(config, signature);
    }

    public String makeAccessTokenFor(UserEntity entity) {
        return make().withSubject(entity).asAccess().build();
    }

    public String makeRefreshToken(UserEntity entity) {
        return make().withSubject(entity).asRefresh().build();
    }

    public UUID parseToUserId(String token) throws JwtTokenException {
        try {
            JWTVerifier verifier = JWT.require(signature)
                    .withIssuer(config.issuer())
                    .build();
            DecodedJWT decodedJWT = verifier.verify(token);
            return UUID.fromString(decodedJWT.getSubject());
        } catch (TokenExpiredException exception) {
            throw new JwtTokenException.Expired(exception, token);
        } catch (JWTDecodeException exception) {
            throw new JwtTokenException.Invalid(exception, token);
        } catch (IllegalArgumentException exception) {
            throw new JwtTokenException.InvalidSubject(exception, token);
        } catch (IncorrectClaimException exception) {
            throw new JwtTokenException.InvalidClaim(exception, token);
        }
    }

    @RequiredArgsConstructor
    public static class Builder {

        private final AuthenticationConfiguration config;
        private final Algorithm signature;
        private String issuer;
        private String subject;
        private Instant expiresAt;

        public Builder withIssuer(String issuer) {
            this.issuer = issuer;
            return this;
        }

        public Builder withSubject(UserEntity subject) {
            return withSubject(subject.getId());
        }

        public Builder withSubject(UUID subject) {
            this.subject = subject.toString();
            return this;
        }

        public Builder withSubject(String subject) {
            this.subject = subject;
            return this;
        }

        public Builder asAccess() {
            this.expiresAt = config.accessExpiration();
            return this;
        }

        public Builder asRefresh() {
            this.expiresAt = config.refreshExpiration();
            return this;
        }

        public Builder withExpiresAt(Instant instant) {
            this.expiresAt = instant;
            return this;
        }

        public String build() {

            if (subject == null || subject.isEmpty()) {
                throw new IllegalArgumentException("subject is required to build token");
            }

            var jwt = JWT.create();
            jwt.withIssuer(issuer == null || issuer.isEmpty() ? config.issuer() : issuer);
            jwt.withSubject(subject);
            jwt.withExpiresAt(expiresAt == null ? config.accessExpiration() : expiresAt);
            return jwt.sign(signature);

        }

    }

}
