package degallant.github.io.todoapp.comments;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.server.core.Relation;

import java.util.UUID;

public class CommentsDto {

    @Getter
    @NoArgsConstructor
    public static class Create {

        @NotBlank
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

    }

}
