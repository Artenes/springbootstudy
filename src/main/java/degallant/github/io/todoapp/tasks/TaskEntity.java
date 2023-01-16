package degallant.github.io.todoapp.tasks;

import degallant.github.io.todoapp.tags.TagEntity;
import jakarta.persistence.*;
import lombok.*;

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

    private boolean complete;

    private Priority priority;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "parent_id")
    private UUID parent;

    @Column(name = "project_id")
    private UUID projectId;

    @Column(name = "due_date")
    private OffsetDateTime dueDate;

    @ManyToMany
    @JoinTable(
            name = "tasks_tags",
            joinColumns = @JoinColumn(name = "task_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private List<TagEntity> tags;

}
