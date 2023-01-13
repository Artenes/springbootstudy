package degallant.github.io.todoapp.comments;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CommentRepository extends JpaRepository<CommentEntity, UUID> {

    List<CommentEntity> findByTaskIdAndUserId(UUID taskId, UUID userId);

}
