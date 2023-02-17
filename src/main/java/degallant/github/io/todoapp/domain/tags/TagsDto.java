package degallant.github.io.todoapp.domain.tags;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.server.core.Relation;

import java.time.OffsetDateTime;
import java.util.UUID;

public class TagsDto {

    @Getter
    @NoArgsConstructor
    public static class Tag {

        private String name;

    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Relation(collectionRelation = "tags")
    public static class Details {

        private UUID id;
        private String name;
        @JsonProperty("created_at")
        private OffsetDateTime createdAt;
        @JsonProperty("updated_at")
        private OffsetDateTime updatedAt;

    }

}
