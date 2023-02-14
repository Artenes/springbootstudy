package degallant.github.io.todoapp.domain.tasks;

import degallant.github.io.todoapp.domain.projects.ProjectEntity;
import degallant.github.io.todoapp.domain.tags.TagEntity;
import degallant.github.io.todoapp.domain.users.UserEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
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

    @Enumerated(value = EnumType.STRING)
    private Priority priority;

    @OneToOne(fetch = FetchType.LAZY)
    private UserEntity user;

    @OneToOne(fetch = FetchType.LAZY)
    private TaskEntity parent;

    @OneToOne(fetch = FetchType.LAZY)
    private ProjectEntity project;

    @Column(name = "due_date")
    private OffsetDateTime dueDate;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "tasks_tags",
            joinColumns = @JoinColumn(name = "task_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private List<TagEntity> tags;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private List<TaskEntity> subTasks;

    public OffsetDateTime getDueDateWithOffset(ZoneOffset zoneOffset) {
        if (dueDate == null) {
            return null;
        }
        return dueDate.withOffsetSameInstant(zoneOffset);
    }

}
