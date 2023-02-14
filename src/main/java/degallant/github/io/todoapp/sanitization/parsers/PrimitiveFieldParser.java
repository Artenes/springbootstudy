package degallant.github.io.todoapp.sanitization.parsers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import degallant.github.io.todoapp.sanitization.InvalidValueException;
import degallant.github.io.todoapp.domain.tasks.Priority;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.*;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

/**
 * @noinspection ClassCanBeRecord
 */
@Component
@RequiredArgsConstructor
public class PrimitiveFieldParser {

    private final ObjectMapper mapper;

    public boolean toBoolean(String value) throws InvalidValueException {
        if ("true".equalsIgnoreCase(value)) {
            return true;
        }

        if ("false".equalsIgnoreCase(value)) {
            return false;
        }

        throw new InvalidValueException("validation.is_boolean", value);
    }

    public int toInteger(String value) throws InvalidValueException {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            throw new InvalidValueException("validation.is_not_a_number", value);
        }
    }

    public UUID toUUID(String value) throws InvalidValueException {
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException exception) {
            throw new InvalidValueException("validation.is_uuid", value);
        }
    }

    public UUID toUuidOrThrow(String value) throws NoSuchElementException {
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException exception) {
            throw new NoSuchElementException("Invalid UUID " + value);
        }
    }

    public LocalDate toLocalDate(String value) throws InvalidValueException {
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException exception) {
            throw new InvalidValueException("validation.is_date", value);
        }
    }

    public OffsetDateTime toOffsetDateTime(String value) throws InvalidValueException {
        try {
            return OffsetDateTime.parse(value);
        } catch (DateTimeParseException exception) {
            throw new InvalidValueException("validation.not_a_valid_date", value);
        }
    }

    public Priority toPriority(String value) throws InvalidValueException {
        var priority = Arrays.stream(Priority.values())
                .filter(p -> p.name().equalsIgnoreCase(value))
                .findFirst();
        if (priority.isEmpty()) {
            throw new InvalidValueException("validation.is_priority", value);
        }
        return priority.get();
    }

    public List<UUID> toUUIDList(String value) throws InvalidValueException {
        try {
            var array = mapper.readValue(value, UUID[].class);
            return Arrays.stream(array).toList();
        } catch (JsonProcessingException exception) {
            throw new InvalidValueException(exception, "validation.invalid_id_list", value);
        }
    }

    public ZoneOffset toTimezone(String value) throws InvalidValueException {
        try {
            return ZoneOffset.of(value);
        } catch (DateTimeException exception) {
            throw new InvalidValueException(exception, "validation.not_a_timezone", value);
        }
    }
}
