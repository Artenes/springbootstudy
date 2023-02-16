package degallant.github.io.todoapp.sanitization;

import lombok.RequiredArgsConstructor;

import java.util.Map;

/**
 * @noinspection ClassCanBeRecord
 */
@RequiredArgsConstructor
public class SanitizedCollection {

    private final Map<String, SanitizedValue> values;

    public SanitizedValue get(String name) {
        return values.get(name);
    }

    public boolean hasAnyFieldWithValue() {
        for (String key : values.keySet()) {
            if (values.get(key).exists()) {
                return true;
            }
        }
        return false;
    }

}
