package degallant.github.io.todoapp.domain.comments;

import degallant.github.io.todoapp.common.BaseEntity;
import degallant.github.io.todoapp.domain.tasks.TaskEntity;
import degallant.github.io.todoapp.domain.users.UserEntity;
import jakarta.persistence.*;
import lombok.*;

@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "comments")
public class CommentEntity extends BaseEntity {

    private String text;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity commenter;

    @OneToOne(fetch = FetchType.LAZY)
    private TaskEntity task;

}
