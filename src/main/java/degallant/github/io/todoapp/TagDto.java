package degallant.github.io.todoapp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

public class TagDto {

    @Getter
    @NoArgsConstructor
    public static class Create {

        private String name;

    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Details {

        private UUID uuid;
        private String name;

    }

}
