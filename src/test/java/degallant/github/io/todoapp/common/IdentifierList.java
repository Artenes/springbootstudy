package degallant.github.io.todoapp.common;

import org.apache.logging.log4j.util.Strings;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @noinspection ClassCanBeRecord
 */
public class IdentifierList {

    private final List<Identifier> list;

    public IdentifierList(List<Identifier> list) {
        this.list = list;
    }

    public List<Identifier> asList() {
        return list;
    }

    public String asString() {
        var ids = list.stream().map(id -> String.format("\"%s\"", id.uuid())).collect(Collectors.toList());
        return String.format("[%s]", Strings.join(ids, ','));
    }

}
