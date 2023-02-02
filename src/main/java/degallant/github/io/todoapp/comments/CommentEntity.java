package degallant.github.io.todoapp.comments;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.domain.Example;

import java.util.UUID;

@Entity(name = "comments")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CommentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String text;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "task_id")
    private UUID taskId;

    public static Example<CommentEntity> belongsTo(UUID id, UUID taskId, UUID userId) {
        return Example.of(CommentEntity.builder().id(id).userId(userId).taskId(taskId).build());
    }

}
