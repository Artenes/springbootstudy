package degallant.github.io.todoapp.domain.users;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class UsersDto {

    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    public static class Details {

        private String id;
        private String name;
        private String email;
        @JsonProperty("picture_url")
        private String pictureUrl;
        private String role;

    }

}
