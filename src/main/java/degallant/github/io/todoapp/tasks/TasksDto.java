package degallant.github.io.todoapp.tasks;

import com.fasterxml.jackson.annotation.JsonProperty;
import degallant.github.io.todoapp.tags.TagsDto;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.server.core.Relation;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class TasksDto {

    @Getter
    @NoArgsConstructor
    public static class Create {

        @NotBlank
        private String title;

        @Size(min = 1, message = "{validation.notempty.message}")
        private String description;

        @JsonProperty("due_date")
        @FutureOrPresent
        private OffsetDateTime dueDate;

        @Pattern(regexp = "^[Pp]1|[Pp]2|[Pp]3$", message = "{validation.priority.message}")
        private String priority;

        @Size(min = 1, message = "{validation.notempty.message}")
        @JsonProperty("tags_ids")
        private Set<UUID> tagsIds;

        @Pattern(regexp = "^[0-9a-fA-F]{8}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{12}$", message = "{validation.uuid.message}")
        @JsonProperty("parent_id")
        private String parentId;

        @Pattern(regexp = "^[0-9a-fA-F]{8}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{12}$", message = "{validation.uuid.message}")
        @JsonProperty("project_id")
        private String projectId;

        private Boolean complete;

        public Priority getPriority() {
            if (priority == null) {
                return null;
            }
            return Arrays.stream(Priority.values())
                    .filter(priority -> priority.name().equalsIgnoreCase(this.priority))
                    .findFirst().orElseThrow();
        }

        public UUID getParentId() {
            if (parentId == null) {
                return null;
            }
            return UUID.fromString(parentId);
        }

        public UUID getProjectId() {
            if (projectId == null) {
                return null;
            }
            return UUID.fromString(projectId);
        }

    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DetailsComplete {

        private String title;
        private String description;
        @JsonProperty("due_date")
        private OffsetDateTime dueDate;
        private Priority priority;
        private URI project;
        private boolean complete;

    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Relation(collectionRelation = "tasks")
    public static class DetailsSimple {

        private UUID id;
        private String title;
        private String description;
        @JsonProperty("due_date")
        private OffsetDateTime dueDate;
        private Boolean complete;

    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Relation(collectionRelation = "subtasks")
    public static class SubTask {

        private UUID id;
        private String title;
        private String description;

    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Relation(itemRelation = "parent")
    public static class ParentTask {

        private UUID id;
        private String title;

    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Update {

        private Boolean complete;
        @JsonProperty("project_id")
        private UUID projectId;

    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Page {

        private int count;
        private long total;
        private int pages;

    }

}
