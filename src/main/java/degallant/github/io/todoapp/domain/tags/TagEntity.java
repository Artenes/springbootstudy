package degallant.github.io.todoapp.domain.tags;

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
@Entity(name = "tags")
public class TagEntity extends BaseEntity {

    private String name;

    @OneToOne(fetch = FetchType.LAZY)
    private UserEntity user;

}
