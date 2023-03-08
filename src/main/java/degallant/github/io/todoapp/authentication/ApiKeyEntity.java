package degallant.github.io.todoapp.authentication;

import degallant.github.io.todoapp.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.*;

import java.time.OffsetDateTime;

@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "api_keys")
public class ApiKeyEntity extends BaseEntity {

    private String secret;

    private String name;

    @Column(name = "last_access")
    private OffsetDateTime lastAccess;

}
