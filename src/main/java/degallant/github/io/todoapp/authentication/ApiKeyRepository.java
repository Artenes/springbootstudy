package degallant.github.io.todoapp.authentication;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ApiKeyRepository extends JpaRepository<ApiKeyEntity, UUID> {

    Optional<ApiKeyEntity> findByIdAndDeletedAtIsNull(UUID id);

    Optional<ApiKeyEntity> findByName(String name);

}
