package degallant.github.io.todoapp.openid;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

@Component
public class OpenIdTokenParser {

    private final GoogleIdTokenVerifier verifier;

    public OpenIdTokenParser(OpenIdConfiguration config) {
        verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                .setAudience(Collections.singletonList(config.googleClientId()))
                .build();
    }

    public OpenIdUser extract(String token) {
        GoogleIdToken idToken;
        try {
            idToken = verifier.verify(token);
        } catch (GeneralSecurityException | IOException | IllegalArgumentException e) {
            throw new OpenIdExtractionException("JWT Extraction failed", e);
        }

        if (idToken == null) {
            throw new OpenIdExtractionException("Extracted JWT token is null");
        }

        GoogleIdToken.Payload payload = idToken.getPayload();
        String email = payload.getEmail();
        String name = (String) payload.get("name");
        String pictureUrl = (String) payload.get("picture");

        return new OpenIdUser(email, name, pictureUrl);
    }

}
