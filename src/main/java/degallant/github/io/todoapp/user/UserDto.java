package degallant.github.io.todoapp.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class UserDto {

    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    public static class Details {

        private String name;
        private String email;
        @JsonProperty("picture_url")
        private String pictureUrl;

    }

}