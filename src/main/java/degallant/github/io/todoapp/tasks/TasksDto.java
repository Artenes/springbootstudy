package degallant.github.io.todoapp.tasks;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.server.core.Relation;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

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

        public Priority getPriorityAsEnum() {
            if (priority == null) {
                return null;
            }
            return Arrays.stream(Priority.values())
                    .filter(priority -> priority.name().equalsIgnoreCase(this.priority))
                    .findFirst().orElseThrow();
        }

        public UUID getParentIdAsUUID() {
            if (parentId == null) {
                return null;
            }
            return UUID.fromString(parentId);
        }

        public UUID getProjectIdAsUUID() {
            if (projectId == null) {
                return null;
            }
            return UUID.fromString(projectId);
        }

        public boolean getCompleteAsBoolean() {
            return Boolean.parseBoolean(complete);
        }

        public List<UUID> getTagsIdsAsUUID() {
            try {
                var mapper = new ObjectMapper();
                var collection = mapper.readValue(tagsIds, String[].class);
                return Arrays.stream(collection).map(UUID::fromString).collect(Collectors.toList());
            } catch (JsonProcessingException exception) {
                return Collections.emptyList();
            }
        }

        public OffsetDateTime getDueDateAsOffsetDateTime() {
            if (dueDate == null) {
                return null;
            }
            return OffsetDateTime.parse(dueDate);
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
