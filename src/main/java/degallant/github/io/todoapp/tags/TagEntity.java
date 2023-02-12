package degallant.github.io.todoapp.tags;

import degallant.github.io.todoapp.users.UserEntity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.domain.Example;

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
