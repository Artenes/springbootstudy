package degallant.github.io.todoapp.common;

import java.net.URI;
import java.util.UUID;

public record Identifier(URI uri) {

    public UUID uuid() {
        var parts = uri.toString().split("/");
        var id = parts[parts.length - 1];
        return UUID.fromString(id);
    }

}
