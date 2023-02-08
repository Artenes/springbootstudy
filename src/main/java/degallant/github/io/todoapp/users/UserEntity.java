package degallant.github.io.todoapp.users;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.UUID;

@Entity(name = "users")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class UserEntity {

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
