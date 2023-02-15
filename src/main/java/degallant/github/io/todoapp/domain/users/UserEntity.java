package degallant.github.io.todoapp.domain.users;

import degallant.github.io.todoapp.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.UUID;

@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "users")
public class UserEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String email;

    private String name;

    @Column(name = "picture_url")
    private String pictureUrl;

    @Enumerated(value = EnumType.STRING)
    private Role role;

    public List<GrantedAuthority> roles() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

}
