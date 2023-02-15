package degallant.github.io.todoapp.domain.projects;

import degallant.github.io.todoapp.common.BaseEntity;
import degallant.github.io.todoapp.domain.users.UserEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToOne;
import lombok.*;

@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "projects")
public class ProjectEntity extends BaseEntity {

    private String title;

    @OneToOne(fetch = FetchType.LAZY)
    private UserEntity user;

}
