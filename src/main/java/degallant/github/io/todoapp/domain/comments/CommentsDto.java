package degallant.github.io.todoapp.domain.comments;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.server.core.Relation;

import java.time.OffsetDateTime;
import java.util.UUID;

public class CommentsDto {

    @Getter
    @NoArgsConstructor
    public static class Create {

        private String text;

    }

    @Getter
    @NoArgsConstructor
    public static class Patch {

        private String text;

    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Relation(collectionRelation = "comments")
    public static class Details {

        private UUID id;
        private String text;
        @JsonProperty("commented_at")
        private OffsetDateTime commentedAt;
        @JsonProperty("edited_at")
        private OffsetDateTime editedAt;

    }

}
