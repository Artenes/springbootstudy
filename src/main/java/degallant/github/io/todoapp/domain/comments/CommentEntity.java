package degallant.github.io.todoapp.domain.comments;

import degallant.github.io.todoapp.domain.tasks.TaskEntity;
import degallant.github.io.todoapp.domain.users.UserEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity(name = "comments")
@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class CommentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String text;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity commenter;

    @OneToOne(fetch = FetchType.LAZY)
    private TaskEntity task;

}
