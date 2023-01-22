package degallant.github.io.todoapp.tags;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.server.core.Relation;

import java.util.UUID;

public class TagsDto {

    @Getter
    @NoArgsConstructor
    public static class Create {

        @NotBlank
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

    }

}
