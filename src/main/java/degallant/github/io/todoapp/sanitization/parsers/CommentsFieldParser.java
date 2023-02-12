package degallant.github.io.todoapp.sanitization.parsers;

import degallant.github.io.todoapp.comments.CommentEntity;
import degallant.github.io.todoapp.comments.CommentsRepository;
import degallant.github.io.todoapp.users.UserEntity;
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
            return repository.findByIdAndUserIdAndTaskId(commentId, user.getId(), taskId).orElseThrow();
        } catch (IllegalArgumentException | NoSuchElementException exception) {
            throw new NoSuchElementException("No comment found with id " + id, exception);
        }
    }

}
