package degallant.github.io.todoapp.tasks;

import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TasksRepository extends JpaRepository<TaskEntity, UUID>, JpaSpecificationExecutor<TaskEntity> {

    List<TaskEntity> findByParentId(UUID uuid);

    Optional<TaskEntity> findByIdAndUserId(UUID id, UUID userId);

    Page<TaskEntity> findByUserId(UUID userId, Pageable pageable, Example<TaskEntity> example);

}
