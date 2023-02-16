package degallant.github.io.todoapp.domain.projects;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.server.core.Relation;

import java.time.OffsetDateTime;
import java.util.UUID;

public class ProjectsDto {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Project {

        private String title;

    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Relation(collectionRelation = "projects", itemRelation = "project")
    public static class Details {

        private UUID id;
        private String title;
        @JsonProperty("created_at")
        private OffsetDateTime createdAt;
        @JsonProperty("updated_at")
        private OffsetDateTime updatedAt;

    }

}
