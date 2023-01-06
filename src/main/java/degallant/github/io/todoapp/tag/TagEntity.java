package degallant.github.io.todoapp.tag;

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

}
