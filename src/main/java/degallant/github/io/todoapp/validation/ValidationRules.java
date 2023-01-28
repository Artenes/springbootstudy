package degallant.github.io.todoapp.validation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import degallant.github.io.todoapp.internationalization.Messages;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

import static degallant.github.io.todoapp.validation.ValidationStatus.*;

/**
 * @noinspection ClassCanBeRecord
 */
@Component
@RequiredArgsConstructor
public class ValidationRules {

    private final ObjectMapper mapper;
    private final Messages messages;

    public ValidationRule isPositive() {
        return value -> {
            try {
                int number = Integer.parseInt(value);
                return withStatus(number > 0, messages.get("validation.is_positive", number));
            } catch (NumberFormatException exception) {
                return withError(messages.get("validation.is_not_a_number", value));
            }
        };
    }

    public ValidationRule isNotEmpty() {
        return value -> withStatus(!value.isEmpty(), messages.get("validation.is_not_empty"));
    }

    public ValidationRule isDate() {
        return value -> {
            var isValid = Pattern.compile("^[12]\\d{3}-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01])$").matcher(value).matches();
            return withStatus(isValid, messages.get("validation.is_date", value));
        };
    }

    public ValidationRule isBoolean() {
        return value -> {
            var isValid = "true".equalsIgnoreCase((String) value) || "false".equals(value);
            return withStatus(isValid, messages.get("validation.is_boolean", value));
        };
    }

    public ValidationRule isPresentOrFuture() {
        return value -> {
            try {
                var time = OffsetDateTime.parse((String) value);
                var now = OffsetDateTime.now();
                return withStatus(time.isAfter(now), messages.get("validation.is_present_or_future", value));
            } catch (DateTimeParseException exception) {
                return withError(messages.get("validation.not_a_valid_date", value));
            }
        };
    }

    public ValidationRule isPriority() {
        return value -> {
            var isValid = Pattern.compile("^[Pp]1|[Pp]2|[Pp]3$").matcher(value).matches();
            return withStatus(isValid, messages.get("validation.is_priority", value));
        };
    }

    public ValidationRule areUuids() {
        return value -> {
            try {
                String[] collection = mapper.readValue(value, String[].class);
                if (collection == null || collection.length == 0) {
                    return withError(messages.get("validation.empty_list"));
                }
                var pattern = Pattern.compile("^[0-9a-fA-F]{8}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{12}$");
                for (int index = 0; index < collection.length; index++) {
                    String uuid = collection[index];
                    if (!pattern.matcher(uuid).matches()) {
                        return withError(messages.get("validation.is_uuid_item", uuid, index));
                    }
                }
                return withValidStatus();
            } catch (JsonProcessingException exception) {
                return withError(messages.get("validation.not_a_valid_array", value));
            }
        };
    }

    public ValidationRule isUuid() {
        return value -> {
            var isValid = Pattern.compile("^[0-9a-fA-F]{8}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{12}$").matcher(value).matches();
            return withStatus(isValid, messages.get("validation.is_uuid", value));
        };
    }

}
