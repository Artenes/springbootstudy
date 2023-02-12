package degallant.github.io.todoapp.sanitization;

import degallant.github.io.todoapp.domain.tags.TagEntity;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Component;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @noinspection ClassCanBeRecord
 */
@Component
@RequiredArgsConstructor
public class FieldValidator {

    public void isPositive(int value) throws InvalidValueException {
        if (value < 0) {
            throwError("validation.is_positive", value);
        }
    }

    public void isNotEmpty(String value) throws InvalidValueException {
        if (value.isEmpty()) {
            throwError("validation.is_empty");
        }
    }

    public void isURL(String value) throws InvalidValueException {
        try {
            new URL(value).toURI();
        } catch (MalformedURLException | URISyntaxException exception) {
            throwError("validation.is_not_url", value);
        }
    }

    public void isPresentOrFuture(OffsetDateTime value) throws InvalidValueException {
        var now = OffsetDateTime.now();
        if (value.isBefore(now)) {
            throwError("validation.is_present_or_future", value);
        }
    }

    public void hasUnknownTag(List<UUID> tagIds, List<TagEntity> tags) throws InvalidValueException {
        var foundIds = tags.stream().map(TagEntity::getId).collect(Collectors.toList());
        var notFound = tagIds.stream().filter(id -> !foundIds.contains(id)).collect(Collectors.toList());
        if (!notFound.isEmpty()) {
            var ids = Strings.join(notFound, ',');
            throwError("validation.do_not_exist_list", ids);
        }
    }

    public InlineValidation check(boolean isValid) {
        return new InlineValidation(isValid);
    }

    public void throwError(String messageId, Object... args) throws InvalidValueException {
        throw new InvalidValueException(messageId, args);
    }

    @RequiredArgsConstructor
    public static class InlineValidation {

        private final boolean isValid;

        public void orThrow(String messageId, Object... args) throws InvalidValueException {
            if (!isValid) {
                throw new InvalidValueException(messageId, args);
            }
        }

    }

}
