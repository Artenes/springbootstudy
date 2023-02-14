package degallant.github.io.todoapp.domain.users;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
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

    @Column(name = "time_zone")
    private ZoneOffset timeZone;

    private String language;

    public List<GrantedAuthority> roles() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    public ZoneOffset getTimeZoneOrDefault() {
        if (timeZone == null) {
            return OffsetDateTime.now().getOffset();
        }
        return timeZone;
    }

}
