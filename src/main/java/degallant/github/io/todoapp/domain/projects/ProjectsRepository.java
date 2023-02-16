package degallant.github.io.todoapp.domain.projects;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectsRepository extends JpaRepository<ProjectEntity, UUID> {

    Optional<ProjectEntity> findByIdAndUserIdAndDeletedAtIsNull(UUID id, UUID userId);

    List<ProjectEntity> findByUserIdAndDeletedAtIsNull(UUID userId);

}
