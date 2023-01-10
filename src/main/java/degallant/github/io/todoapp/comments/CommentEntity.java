package degallant.github.io.todoapp.comments;

import jakarta.persistence.*;
import lombok.*;

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

    @Column(name = "todo_id")
    private UUID todoId;

}
