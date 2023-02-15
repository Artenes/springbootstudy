package degallant.github.io.todoapp.sanitization;

import lombok.RequiredArgsConstructor;

/**
 * @noinspection ClassCanBeRecord, unchecked
 */
@RequiredArgsConstructor
public class SanitizedValue {

    private final Object object;

    public Integer asInt() {
        return (Integer) object;
    }

    public boolean asBool() {
        return object != null && (boolean) object;
    }

    public <T> T or(T defaultValue) {
        return object == null ? defaultValue : (T) object;
    }

    public <T> T value() {
        return (T) object;
    }

    public <T> T ifNull(T defaultValue) {
        return object == null ? defaultValue : (T) object;
    }

}
