package degallant.github.io.todoapp.tags;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TagsRepository extends JpaRepository<TagEntity, UUID> {

    Optional<TagEntity> findByIdAndUserId(UUID id, UUID userId);

    List<TagEntity> findByUserId(UUID userId);

}
