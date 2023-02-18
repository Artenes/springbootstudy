package degallant.github.io.todoapp.sanitization;

import lombok.RequiredArgsConstructor;

import java.util.function.Consumer;

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

    public <T> T as(Class<T> type) {
        return (T) object;
    }

    public <T> T ifNull(T defaultValue) {
        return object == null ? defaultValue : (T) object;
    }

    public <T> void consumeIfExists(Consumer<T> consumer) {
        if (object != null) {
            consumer.accept((T) object);
        }
    }

    public <T> void consumeIfExistsAs(Class<T> type, Consumer<T> consumer) {
        if (object != null) {
            consumer.accept((T) object);
        }
    }

    public boolean exists() {
        return object != null;
    }

}
