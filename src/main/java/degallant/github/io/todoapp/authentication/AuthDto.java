package degallant.github.io.todoapp.authentication;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TokenPair {

        @JsonProperty("access_token")
        private String accessToken;

        @JsonProperty("refresh_token")
        private String refreshToken;

    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Patch {

        private String name;

        @JsonProperty("picture_url")
        private String pictureUrl;

        private String role;

        @JsonProperty("time_zone")
        private String timeZone;

    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Promote {

        @JsonProperty("user_id")
        private String userId;

    }

}
