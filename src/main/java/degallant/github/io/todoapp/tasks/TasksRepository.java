package degallant.github.io.todoapp.tasks;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TasksRepository extends JpaRepository<TaskEntity, UUID> {

    List<TaskEntity> findByParentId(UUID uuid);

    Optional<TaskEntity> findByIdAndUserId(UUID id, UUID userId);

    List<TaskEntity> findByUserId(UUID userId);

}
