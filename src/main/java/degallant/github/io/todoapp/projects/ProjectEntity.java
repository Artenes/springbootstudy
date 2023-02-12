package degallant.github.io.todoapp.projects;

import degallant.github.io.todoapp.users.UserEntity;
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

    @OneToOne(fetch = FetchType.LAZY)
    private UserEntity user;

}
