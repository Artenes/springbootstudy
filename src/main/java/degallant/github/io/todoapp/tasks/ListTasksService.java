package degallant.github.io.todoapp.tasks;

import degallant.github.io.todoapp.common.PagedResponse;
import degallant.github.io.todoapp.common.SortingParser;
import degallant.github.io.todoapp.common.Time;
import degallant.github.io.todoapp.users.UserEntity;
import degallant.github.io.todoapp.validation.FieldParser;
import degallant.github.io.todoapp.validation.FieldValidator;
import degallant.github.io.todoapp.validation.SanitizedField;
import degallant.github.io.todoapp.validation.Sanitizer;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static degallant.github.io.todoapp.common.LinkBuilder.makeLink;
import static degallant.github.io.todoapp.common.LinkBuilder.makeLinkTo;

/**
 * @noinspection ClassCanBeRecord
 */
@Service
@RequiredArgsConstructor
public class ListTasksService {

    private final SortingParser sortingParser;
    private final TasksRepository tasksRepository;
    private final Sanitizer sanitizer;
    private final FieldValidator rules;
    private final FieldParser parser;
    private final PagedResponse pagedResponse;

    public RepresentationModel<?> list(
            String page,
            String sort,
            String title,
            String dueDate,
            String complete,
            UserEntity user
    ) {

        var result = sanitizeFields(page, sort, title, dueDate, complete);

        var linkBuilder = makeLink("v1", "tasks").addSort(sort);
        var pageRequest = PageRequest.of(result.get("p").asInt() - 1, 10, result.get("s").or(Sort.unsorted()));
        var specification = matchesAnyOf(user.getId(), title, result.get("complete").value(), dueDate);
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
                    var linkSelf = makeLinkTo("v1", "tasks", entity.getId()).withSelfRel();
                    var linkComments = makeLinkTo("v1", "tasks", entity.getId(), "comments").withRel("comments");
                    return EntityModel.of(task).add(linkSelf, linkComments);
                })
                .collect(Collectors.toList());

        response.embed(tasks, TasksDto.DetailsSimple.class);

        return response.build();
    }

    public Specification<TaskEntity> matchesAnyOf(UUID userId, String title, Boolean complete, String date) {
        return (root, query, builder) -> {

            List<Predicate> predicates = new ArrayList<>();

            predicates.add(builder.equal(root.get("userId"), userId));

            if (title != null && !title.isEmpty()) {
                predicates.add(builder.like(root.get("title"), "%" + title + "%"));
            }

            if (complete != null) {
                predicates.add(builder.equal(root.get("complete"), complete));
            }

            if (date != null && !date.isEmpty()) {
                var time = Time.from(date);
                predicates.add(builder.between(root.get("dueDate"), time.startOfDay(), time.endOfDay()));
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
                        .sanitize(value -> parser.toSort(value, "title", "due_date")),

                sanitizer.field("title").withOptionalValue(title).sanitize(value -> {
                    rules.isNotEmpty(title);
                    return value;
                }),

                sanitizer.field("due_date").withOptionalValue(dueDate).sanitize(parser::toLocalDate),

                sanitizer.field("complete").withOptionalValue(complete).sanitize(parser::toBoolean)
        );
    }

}