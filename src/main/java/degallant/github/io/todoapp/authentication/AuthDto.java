package degallant.github.io.todoapp.authentication;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.OverridesAttribute;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class AuthDto {

    @Getter
    @NoArgsConstructor
    public static class Authenticate {

        @JsonProperty("open_id_token")
        private String openIdToken;

    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TokenPair {

        @JsonProperty("access_token")
        private String accessToken;

        @JsonProperty("refresh_token")
        private String refreshToken;

    }

}
