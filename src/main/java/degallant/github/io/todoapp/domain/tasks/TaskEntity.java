package degallant.github.io.todoapp.domain.tasks;

import degallant.github.io.todoapp.common.BaseEntity;
import degallant.github.io.todoapp.domain.comments.CommentEntity;
import degallant.github.io.todoapp.domain.projects.ProjectEntity;
import degallant.github.io.todoapp.domain.tags.TagEntity;
import degallant.github.io.todoapp.domain.users.UserEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.Where;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "tasks")
public class TaskEntity extends BaseEntity {

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

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id")
    @Where(clause = "deleted_at IS NULL")
    private List<CommentEntity> comments;

}
