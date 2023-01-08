package degallant.github.io.todoapp.todo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TodoRepository extends JpaRepository<TodoEntity, UUID> {

    List<TodoEntity> findByParent(UUID uuid);

    Optional<TodoEntity> findByIdAndUserId(UUID id, UUID userId);

    List<TodoEntity> findByUserId(UUID userId);

}
