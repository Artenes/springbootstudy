package degallant.github.io.todoapp.tag;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TagRepository extends JpaRepository<TagEntity, UUID> {

    Optional<TagEntity> findByIdAndUserId(UUID id, UUID userId);

}