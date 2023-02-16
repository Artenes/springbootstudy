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

    public CommentEntity toCommentOrThrowNoSuchElement(String taskId, String commentId, UserEntity user) throws NoSuchElementException {
        try {
            return repository.findBy(UUID.fromString(taskId), UUID.fromString(commentId), user.getId()).orElseThrow();
        } catch (IllegalArgumentException | NoSuchElementException exception) {
            throw new NoSuchElementException(String.format("No comment found with the ids task:%s, comment:%s, user:%s", taskId, commentId, user.getId()), exception);
        }
    }

}
