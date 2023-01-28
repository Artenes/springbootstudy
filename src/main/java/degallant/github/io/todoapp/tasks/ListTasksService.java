package degallant.github.io.todoapp.tasks;

import degallant.github.io.todoapp.common.SortingParser;
import degallant.github.io.todoapp.common.Time;
import degallant.github.io.todoapp.users.UserEntity;
import degallant.github.io.todoapp.validation.ValidationRules;
import degallant.github.io.todoapp.validation.Validator;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.mediatype.hal.HalModelBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static degallant.github.io.todoapp.common.LinkBuilder.makeLink;
import static degallant.github.io.todoapp.common.LinkBuilder.makeLinkTo;
import static degallant.github.io.todoapp.validation.Validation.field;

/**
 * @noinspection ClassCanBeRecord
 */
@Service
@RequiredArgsConstructor
public class ListTasksService {

    private final Validator validator;
    private final ValidationRules rules;
    private final SortingParser sortingParser;
    private final TasksRepository tasksRepository;

    public RepresentationModel<?> list(
            String requestedPageNumber,
            String sort,
            String title,
            String dueDate,
            String requestedComplete,
            Authentication authentication
    ) {

        validator.validate(
                field("p", requestedPageNumber, rules.isPositive()),
                field("s", sort, rules.isNotEmpty()),
                field("title", title, rules.isNotEmpty()),
                field("due_date", dueDate, rules.isDate()),
                field("complete", requestedComplete, rules.isBoolean())
        );

        var pageNumber = Integer.parseInt(requestedPageNumber);
        var complete = requestedComplete == null ? null : Boolean.valueOf(requestedComplete);

        var response = HalModelBuilder.emptyHalModel();
        var userId = ((UserEntity) authentication.getPrincipal()).getId();

        var linkBuilder = makeLink("v1", "tasks").addSort(sort);
        var sortInformation = sortingParser.parse(sort, "title", "due_date");
        var pageRequest = PageRequest.of(pageNumber - 1, 10, sortInformation);

        var specification = matchesAnyOf(userId, title, complete, dueDate);
        var page = tasksRepository.findAll(specification, pageRequest);

        response.entity(TasksDto.Page.builder()
                .count(page.getNumberOfElements())
                .pages(page.getTotalPages())
                .total(page.getTotalElements())
                .build());

        response.link(linkBuilder.addPage(pageNumber).build().withSelfRel());

        if (page.hasNext()) {
            response.link(linkBuilder.addPage(pageNumber + 1).build().withRel("next"));
        }

        if (page.hasPrevious()) {
            response.link(linkBuilder.addPage(pageNumber - 1).build().withRel("previous"));
        }

        if (!page.isEmpty()) {
            response.link(linkBuilder.addPage(1).build().withRel("first"));
            response.link(linkBuilder.addPage(page.getTotalPages()).build().withRel("last"));
        }

        var tasks = page
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

}