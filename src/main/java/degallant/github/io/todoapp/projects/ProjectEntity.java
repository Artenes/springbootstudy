package degallant.github.io.todoapp.projects;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.domain.Example;

import java.util.UUID;

@Entity(name = "projects")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ProjectEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String title;

    @Column(name = "user_id")
    private UUID userId;

    public static Example<ProjectEntity> belongsTo(UUID id, UUID userId) {
        return Example.of(ProjectEntity.builder().id(id).userId(userId).build());
    }

}
