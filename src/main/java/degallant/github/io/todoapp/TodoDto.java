package degallant.github.io.todoapp;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

public class TodoDto {

    @Getter
    @NoArgsConstructor
    public static class Create {

        private String title;
        private String description;
        @JsonProperty("due_date")
        private OffsetDateTime dueDate;
        private Priority priority;

    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Details {

        private String title;
        private String description;
        @JsonProperty("due_date")
        private OffsetDateTime dueDate;
        private Priority priority;

    }

    public record PatchComplete(boolean complete) {
    }

}
