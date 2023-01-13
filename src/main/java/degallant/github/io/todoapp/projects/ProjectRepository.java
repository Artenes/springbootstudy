package degallant.github.io.todoapp.projects;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ProjectRepository extends JpaRepository<ProjectEntity, UUID> {

    Optional<ProjectEntity> findByIdAndUserId(UUID id, UUID userId);

}
