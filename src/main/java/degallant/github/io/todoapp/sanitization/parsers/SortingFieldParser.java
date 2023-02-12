package degallant.github.io.todoapp.sanitization.parsers;

import com.google.common.base.CaseFormat;
import degallant.github.io.todoapp.sanitization.InvalidValueException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class SortingFieldParser {

    public Sort toSortOrThrowInvalidValue(String value, String... validAttributes) throws InvalidValueException {
        try {
            return parse(value, validAttributes);
        } catch (SortParsingException exception) {
            if (exception instanceof SortParsingException.InvalidAttribute) {
                var attribute = ((SortParsingException.InvalidAttribute) exception).getAttribute();
                throw new InvalidValueException("error.invalid_sort_attribute.detail", attribute);
            } else if (exception instanceof SortParsingException.InvalidDirection) {
                var direction = ((SortParsingException.InvalidDirection) exception).getDirection();
                throw new InvalidValueException("error.invalid_sort_direction.detail", direction);
            } else {
                var query = ((SortParsingException.InvalidQuery) exception).getQuery();
                throw new InvalidValueException("error.invalid_sort_query.detail", query);
            }
        }
    }

    /**
     * Parses a string query into a valid Sort instance.
     *
     * @param query a query in the format name:asc,email:desc,address:asc
     * @param attributes the list of valid attributes that can be sorted
     * @return a valid sort from the provided query, an unsorted sort if query is null or empty
     *
     * @throws SortParsingException in case of an invalid sort is provided
     */
    private Sort parse(String query, String... attributes) throws SortParsingException {
        Sort sort = null;
        Set<String> validAttributes = Set.of(attributes);
        var properties = query.split(",");
        for (String property : properties) {
            String[] parts = property.split(":");

            if (parts.length <= 1) {
                throw new SortParsingException.InvalidQuery(property);
            }

            String name = parts[0];
            String type = parts[1];

            if (!"asc".equals(type) && !"desc".equals(type)) {
                throw new SortParsingException.InvalidDirection(type);
            }

            if (!validAttributes.contains(name)) {
                throw new SortParsingException.InvalidAttribute(name);
            }

            var propertySort = Sort.by(toCamelCase(name));
            if ("asc".equals(type)) {
                propertySort = propertySort.ascending();
            } else {
                propertySort = propertySort.descending();
            }

            if (sort == null) {
                sort = propertySort;
            } else {
                sort = sort.and(propertySort);
            }
        }

        return sort;
    }

    private String toCamelCase(String snakeCase) {
        return CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, snakeCase);
    }

}
