package degallant.github.io.todoapp.test;

import org.apache.logging.log4j.util.Strings;

import java.net.URI;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public record Identifier(URI uri) {

    public UUID uuid() {
        var parts = uri.toString().split("/");
        var id = parts[parts.length - 1];
        return UUID.fromString(id);
    }

    /**
     * @noinspection ClassCanBeRecord
     */
    public static class Collection {

        private final List<Identifier> list;

        public Collection(List<Identifier> list) {
            this.list = list;
        }

        public List<Identifier> asList() {
            return list;
        }

        public Identifier get(int index) {
            return list.get(index);
        }

        public String asString() {
            var ids = list.stream().map(id -> String.format("\"%s\"", id.uuid())).collect(Collectors.toList());
            return String.format("[%s]", Strings.join(ids, ','));
        }

    }

}
