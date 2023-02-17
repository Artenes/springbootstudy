package degallant.github.io.todoapp.domain.tags;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TagsRepository extends JpaRepository<TagEntity, UUID> {

    Optional<TagEntity> findByIdAndUserIdAndDeletedAtIsNull(UUID id, UUID userId);

    List<TagEntity> findByUserIdAndDeletedAtIsNull(UUID userId);

    //TODO update this to new soft delete rules
    @Query(value = "select * from tags where user_id = :userId and id in :ids", nativeQuery = true)
    List<TagEntity> findAllByUserIdAndId(UUID userId, Iterable<UUID> ids);

}
