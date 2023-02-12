package degallant.github.io.todoapp.domain.tags;

import degallant.github.io.todoapp.domain.users.UserEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity(name = "tags")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TagEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String name;

    @OneToOne(fetch = FetchType.LAZY)
    private UserEntity user;

}
