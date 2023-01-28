package degallant.github.io.todoapp.validation;

import java.util.NoSuchElementException;
import java.util.UUID;

public class PathValidator {

    public static UUID parseUUIDOrFail(String id) {
        try {
            return UUID.fromString(id);
        } catch (IllegalArgumentException exception) {
            throw new NoSuchElementException("Invalid UUID " + id);
        }
    }

}
