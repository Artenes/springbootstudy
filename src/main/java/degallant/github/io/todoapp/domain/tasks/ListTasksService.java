package degallant.github.io.todoapp.domain.tasks;

import degallant.github.io.todoapp.common.LinkBuilder;
import degallant.github.io.todoapp.common.PagedResponse;
import degallant.github.io.todoapp.sanitization.parsers.SortingFieldParser;
import degallant.github.io.todoapp.domain.users.UserEntity;
import degallant.github.io.todoapp.sanitization.parsers.PrimitiveFieldParser;
import degallant.github.io.todoapp.sanitization.FieldValidator;
import degallant.github.io.todoapp.sanitization.SanitizedField;
import degallant.github.io.todoapp.sanitization.Sanitizer;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @noinspection ClassCanBeRecord
 */
@Service
@RequiredArgsConstructor
public class ListTasksService {

    private final TasksRepository tasksRepository;
    private final Sanitizer sanitizer;
    private final FieldValidator rules;
    private final PrimitiveFieldParser parser;
    private final SortingFieldParser sortingParser;
    private final PagedResponse pagedResponse;
    private final LinkBuilder link;

    public RepresentationModel<?> list(
            String page,
            String sort,
            String title,
            String dueDate,
            String complete,
            UserEntity user
    ) {

        var result = sanitizeFields(page, sort, title, dueDate, complete);

        var linkBuilder = link.to("tasks").withParams().addSort(sort);
        var pageRequest = PageRequest.of(result.get("p").asInt() - 1, 10, result.get("s").or(Sort.unsorted()));
        var specification = matchesAnyOf(user, title, result.get("complete").value(), result.get("due_date").value());
        var tasksPage = tasksRepository.findAll(specification, pageRequest);

        var response = pagedResponse.makePagedResponse(linkBuilder, tasksPage, result.get("p").value());

        var tasks = tasksPage
                .stream()
                .map(entity -> {
                    var task = TasksDto.DetailsSimple.builder()
                            .id(entity.getId())
                            .title(entity.getTitle())
                            .description(entity.getDescription())
                            .dueDate(entity.getDueDate())
                            .complete(entity.getComplete())
                            .build();
                    var linkSelf = link.to("tasks").slash(entity.getId()).withSelfRel();
                    var linkComments = link.to("tasks").slash(entity.getId()).slash("comments").withRel("comments");
                    return EntityModel.of(task).add(linkSelf, linkComments);
                })
                .collect(Collectors.toList());

        response.embed(tasks, TasksDto.DetailsSimple.class);

        return response.build();
    }

    public Specification<TaskEntity> matchesAnyOf(UserEntity user, String title, Boolean complete, LocalDate date) {
        return (root, query, builder) -> {

            List<Predicate> predicates = new ArrayList<>();

            predicates.add(builder.equal(root.get("user"), user));

            if (title != null && !title.isEmpty()) {
                predicates.add(builder.like(root.get("title"), "%" + title + "%"));
            }

            if (complete != null) {
                predicates.add(builder.equal(root.get("complete"), complete));
            }

            if (date != null) {
                var now = OffsetDateTime.now();
                var startOfDay = date.atTime(0, 0).atOffset(now.getOffset());
                var endOfDay = date.atTime(23, 59).atOffset(now.getOffset());
                predicates.add(builder.between(root.get("dueDate"), startOfDay, endOfDay));
            }

            return builder.and(predicates.toArray(new Predicate[]{}));

        };
    }

    private Map<String, SanitizedField> sanitizeFields(String page, String sort, String title, String dueDate, String complete) {
        return sanitizer.sanitize(
                sanitizer.field("p").withOptionalValue(page).sanitize(value -> {
                    var parsed = parser.toInteger(value);
                    rules.isPositive(parsed);
                    return parsed;
                }),

                sanitizer.field("s").withOptionalValue(sort)
                        .sanitize(value -> sortingParser.toSortOrThrowInvalidValue(value, "title", "due_date")),

                sanitizer.field("title").withOptionalValue(title).sanitize(value -> {
                    rules.isNotEmpty(title);
                    return value;
                }),

                sanitizer.field("due_date").withOptionalValue(dueDate).sanitize(parser::toLocalDate),

                sanitizer.field("complete").withOptionalValue(complete).sanitize(parser::toBoolean)
        );
    }

}