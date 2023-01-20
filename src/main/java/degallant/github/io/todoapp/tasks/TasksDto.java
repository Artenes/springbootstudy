package degallant.github.io.todoapp.tasks;

import com.fasterxml.jackson.annotation.JsonProperty;
import degallant.github.io.todoapp.tags.TagsDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.server.core.Relation;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class TasksDto {

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
        private UUID project;

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

}
