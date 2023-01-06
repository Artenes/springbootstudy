package degallant.github.io.todoapp.todo;

import com.fasterxml.jackson.annotation.JsonProperty;
import degallant.github.io.todoapp.tag.TagDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class TodoDto {

    @Getter
    @NoArgsConstructor
    public static class Create {

        private String title;
        private String description;
        @JsonProperty("due_date")
        private OffsetDateTime dueDate;
        private Priority priority;
        private Set<UUID> tags;
        private UUID parent;

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
        private URI parent;
        private List<URI> children;
        private List<TagDto.Details> tags;

    }

    public record PatchComplete(boolean complete) {
    }

}
