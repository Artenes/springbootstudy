package degallant.github.io.todoapp.domain.tasks;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.server.core.Relation;

import java.time.OffsetDateTime;
import java.util.UUID;

public class TasksDto {

    @Getter
    @NoArgsConstructor
    public static class Create {

        private String title;
        private String description;
        @JsonProperty("due_date")
        private String dueDate;
        private String priority;
        @JsonProperty("tags_ids")
        private String tagsIds;
        @JsonProperty("parent_id")
        private String parentId;
        @JsonProperty("project_id")
        private String projectId;
        private String complete;

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
        private boolean complete;
        @JsonProperty("created_at")
        private OffsetDateTime createdAt;
        @JsonProperty("updated_at")
        private OffsetDateTime updatedAt;

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
    public static class Page {

        private int count;
        private long total;
        private int pages;

    }

}
