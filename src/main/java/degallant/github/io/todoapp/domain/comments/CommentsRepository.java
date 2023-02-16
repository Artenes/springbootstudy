package degallant.github.io.todoapp.domain.comments;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CommentsRepository extends JpaRepository<CommentEntity, UUID> {

    @Query(nativeQuery = true, value = "SELECT c.* FROM comments c INNER JOIN tasks t ON c.task_id = t.id WHERE t.id = :taskId AND c.id = :commentId AND t.user_id = :userId AND c.deleted_at IS NULL AND t.deleted_at IS NULL")
    Optional<CommentEntity> findBy(UUID taskId, UUID commentId, UUID userId);

    List<CommentEntity> findByTaskIdAndCommenterId(UUID taskId, UUID commenterId);

    Optional<CommentEntity> findByIdAndCommenterIdAndTaskId(UUID id, UUID commenterId, UUID taskId);

}
