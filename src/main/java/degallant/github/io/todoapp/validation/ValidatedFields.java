package degallant.github.io.todoapp.validation;

import java.util.HashMap;
import java.util.Map;

public class ValidatedFields {

    private final Map<String, SanitizedField> fields = new HashMap<>();

    public SanitizedField get(String name) {
        return fields.get(name);
    }

}
