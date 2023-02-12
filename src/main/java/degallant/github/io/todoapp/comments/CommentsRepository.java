package degallant.github.io.todoapp.comments;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CommentsRepository extends JpaRepository<CommentEntity, UUID> {

    List<CommentEntity> findByTaskIdAndCommenterId(UUID taskId, UUID commenterId);

    Optional<CommentEntity> findByIdAndCommenterIdAndTaskId(UUID id, UUID commenterId, UUID taskId);

}
