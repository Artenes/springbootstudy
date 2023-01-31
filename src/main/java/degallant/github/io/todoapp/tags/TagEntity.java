package degallant.github.io.todoapp.tags;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.domain.Example;

import java.util.UUID;

@Entity(name = "tags")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TagEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String name;

    @Column(name = "user_id")
    private UUID userId;

    public static Example<TagEntity> belongsTo(UUID id, UUID userId) {
        return Example.of(TagEntity.builder().id(id).userId(userId).build());
    }

}
