package degallant.github.io.todoapp.sanitization.parsers;

import degallant.github.io.todoapp.domain.comments.CommentEntity;
import degallant.github.io.todoapp.domain.comments.CommentsRepository;
import degallant.github.io.todoapp.domain.users.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.NoSuchElementException;
import java.util.UUID;

/** @noinspection ClassCanBeRecord*/
@Component
@RequiredArgsConstructor
public class CommentsFieldParser {

    private final CommentsRepository repository;

    public CommentEntity toCommentOrThrowNoSuchElement(String id, UUID taskId, UserEntity user) throws NoSuchElementException {
        try {
            var commentId = UUID.fromString(id);
            return repository.findByIdAndCommenterIdAndTaskId(commentId, user.getId(), taskId).orElseThrow();
        } catch (IllegalArgumentException | NoSuchElementException exception) {
            throw new NoSuchElementException("No comment found with id " + id, exception);
        }
    }

}
