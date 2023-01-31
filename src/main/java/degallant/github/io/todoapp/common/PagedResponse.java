package degallant.github.io.todoapp.common;

import org.springframework.data.domain.Page;
import org.springframework.hateoas.mediatype.hal.HalModelBuilder;
import org.springframework.stereotype.Component;

@Component
public class PagedResponse {

    public HalModelBuilder makePagedResponse(LinkBuilder linkBuilder, Page<?> entityPage, int page) {
        var builder = HalModelBuilder.emptyHalModel();

        var body = new PageBody(
                entityPage.getNumberOfElements(),
                entityPage.getTotalPages(),
                entityPage.getTotalElements()
        );

        builder.entity(body);
        builder.link(linkBuilder.addPage(page).build().withSelfRel());

        if (entityPage.hasNext()) {
            builder.link(linkBuilder.addPage(page + 1).build().withRel("next"));
        }

        if (entityPage.hasPrevious()) {
            builder.link(linkBuilder.addPage(page - 1).build().withRel("previous"));
        }

        if (!entityPage.isEmpty()) {
            builder.link(linkBuilder.addPage(1).build().withRel("first"));
            builder.link(linkBuilder.addPage(entityPage.getTotalPages()).build().withRel("last"));
        }

        return builder;
    }

    public static record PageBody(int count, int pages, long total) {

    }

}
