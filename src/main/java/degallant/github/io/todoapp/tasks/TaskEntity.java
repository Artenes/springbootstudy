package degallant.github.io.todoapp.tasks;

import degallant.github.io.todoapp.tags.TagEntity;
import degallant.github.io.todoapp.users.UserEntity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.domain.Example;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Entity(name = "tasks")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TaskEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String title;

    private String description;

    private Boolean complete;

    private Priority priority;

    @OneToOne(fetch = FetchType.LAZY)
    private UserEntity user;

    @Column(name = "parent_id")
    private UUID parentId;

    @Column(name = "project_id")
    private UUID projectId;

    @Column(name = "due_date")
    private OffsetDateTime dueDate;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "tasks_tags",
            joinColumns = @JoinColumn(name = "task_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private List<TagEntity> tags;

    public static Example<TaskEntity> belongsTo(UUID id, UserEntity user) {
        return Example.of(TaskEntity.builder().id(id).user(user).build());
    }

}
