package degallant.github.io.todoapp.validation;

/**
 * @noinspection ClassCanBeRecord, unchecked
 */
public class SanitizedField {

    private final Object object;

    public SanitizedField(Object object) {
        this.object = object;
    }

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
