package degallant.github.io.todoapp.projects;

import jakarta.persistence.*;
import lombok.*;

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

}
