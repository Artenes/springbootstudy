package degallant.github.io.todoapp.domain;

import degallant.github.io.todoapp.test.IntegrationTest;
import org.junit.jupiter.api.Test;

import java.util.UUID;

public class TagsTests extends IntegrationTest {

    //region fails

    @Test
    public void get_fails_resource_access() {

        var tag = entityRequest.asUser(DEFAULT_USER).makeTag("Tag A");
        var anotherTag = entityRequest.asUser(ANOTHER_USER).makeTag("Tag X");

        request.asUser(DEFAULT_USER).to("tags/invalid").get().isNotFound();
        request.asUser(DEFAULT_USER).to("tags/" + UUID.randomUUID()).get().isNotFound();
        request.asUser(DEFAULT_USER).to(anotherTag.uri()).get().isNotFound();

        request.asUser(DEFAULT_USER).to(tag.uri()).delete().isNoContent();
        request.asUser(DEFAULT_USER).to(tag.uri()).get().isNotFound();

    }

    @Test
    public void patch_fails_resource_access() {

        var tag = entityRequest.asUser(DEFAULT_USER).makeTag("Tag A");
        var anotherTag = entityRequest.asUser(ANOTHER_USER).makeTag("Tag X");

        request.asUser(DEFAULT_USER).to("tags/invalid").withField("name", "new name").patch().isNotFound();
        request.asUser(DEFAULT_USER).to("tags/" + UUID.randomUUID()).withField("name", "new name").patch().isNotFound();
        request.asUser(DEFAULT_USER).to(anotherTag.uri()).withField("name", "new name").patch().isNotFound();

        request.asUser(DEFAULT_USER).to(tag.uri()).delete().isNoContent();
        request.asUser(DEFAULT_USER).to(tag.uri()).withField("name", "new name").patch().isNotFound();

    }

    @Test
    public void delete_fails_resource_access() {

        var tag = entityRequest.asUser(DEFAULT_USER).makeTag("Tag A");
        var anotherTag = entityRequest.asUser(ANOTHER_USER).makeTag("Tag X");

        request.asUser(DEFAULT_USER).to("tags/invalid").delete().isNotFound();
        request.asUser(DEFAULT_USER).to("tags/" + UUID.randomUUID()).delete().isNotFound();
        request.asUser(DEFAULT_USER).to(anotherTag.uri()).delete().isNotFound();

        request.asUser(DEFAULT_USER).to(tag.uri()).delete().isNoContent();
        request.asUser(DEFAULT_USER).to(tag.uri()).delete().isNotFound();

    }

    @Test
    public void post_fails_invalid_body() {

        request.asUser(DEFAULT_USER).to("tags")
                .withField("name", "")
                .post().isBadRequest()
                .hasField("$.errors[0].type", contains("validation.is_empty"));

        request.asUser(DEFAULT_USER).to("tags")
                .withField("random", "value")
                .post().isBadRequest()
                .hasField("$.errors[0].type", contains("validation.is_required"));

    }

    @Test
    public void patch_fails_invalid_body() {

        var tag = entityRequest.asUser(DEFAULT_USER).makeTag("Tag A");

        request.asUser(DEFAULT_USER).to(tag.uri())
                .withField("name", "")
                .patch().isBadRequest()
                .hasField("$.errors[0].type", contains("validation.is_empty"));

    }

    @Test
    public void get_fails_list_ignore_others() {

        entityRequest.asUser(DEFAULT_USER).makeTags("Tag A", "Tag C");

        entityRequest.asUser(ANOTHER_USER).makeTag("Tag B");

        request.asUser(DEFAULT_USER).to("tags")
                .get().isOk()
                .hasField("$._embedded.tags.length()", isEqualTo(2))
                .hasField("$._embedded.tags[?(@.name == 'Tag A')]", exists())
                .hasField("$._embedded.tags[?(@.name == 'Tag C')]", exists());

    }

    //endregion

    //region edges

    @Test
    public void patch_edge_no_update() {

        var tag = entityRequest.asUser(DEFAULT_USER).makeTag("Tag A");

        request.asUser(DEFAULT_USER).to(tag.uri())
                .withField("random", "value")
                .patch().isNoContent();

    }

    @Test
    public void get_edge_list_no_item() {

        request.asUser(DEFAULT_USER).to("tags")
                .get().isOk()
                .hasField("$._embedded.tags.length()", isEqualTo(0));

    }

    @Test
    public void get_edge_list_ignore_deleted() {

        var tags = entityRequest.asUser(DEFAULT_USER).makeTags("Tag A", "Tag B", "Tag C");

        request.asUser(DEFAULT_USER).to(tags.get(1).uri()).delete().isNoContent();

        request.asUser(DEFAULT_USER).to("tags")
                .get().isOk()
                .hasField("$._embedded.tags.length()", isEqualTo(2))
                .hasField("$._embedded.tags[?(@.name == 'Tag A')]", exists())
                .hasField("$._embedded.tags[?(@.name == 'Tag C')]", exists());

    }

    @Test
    public void get_edge_list_display_offset() {

        var tag = entityRequest.asUser(DEFAULT_USER).makeTag("Tag A");
        request.asUser(DEFAULT_USER).to(tag.uri()).withField("name", "new name").patch().isOk();

        request.asUser(DEFAULT_USER).to("tags")
                .withHeader("Accept-Offset", "+04:00")
                .get().isOk()
                .hasField("$._embedded.tags[0].created_at", contains("+04:00"))
                .hasField("$._embedded.tags[0].updated_at", contains("+04:00"));

    }

    @Test
    public void get_edge_display_offset() {

        var tag = entityRequest.asUser(DEFAULT_USER).makeTag("Tag A");
        request.asUser(DEFAULT_USER).to(tag.uri()).withField("name", "new name").patch().isOk();

        request.asUser(DEFAULT_USER).to(tag.uri()).get().isOk().getBody();

        request.asUser(DEFAULT_USER).to(tag.uri())
                .withHeader("Accept-Offset", "+04:00")
                .get().isOk()
                .hasField("$.created_at", contains("+04:00"))
                .hasField("$.updated_at", contains("+04:00"));

    }

    //endregion

    //region success

    @Test
    public void get_success() {

        var tag = entityRequest.asUser(DEFAULT_USER).makeTag("Tag A");

        request.asUser(DEFAULT_USER).to(tag.uri())
                .get().isOk()
                .hasField("$.name", isEqualTo("Tag A"))
                .hasField("$.created_at", existsAndNotNull())
                .hasField("$.updated_at", exists())
                .hasField("$._links.self.href", contains("/tags/"))
                .hasField("$._links.all.href", contains("/tags"));


    }

    @Test
    public void patch_success() {

        var tag = entityRequest.asUser(DEFAULT_USER).makeTag("Tag A");

        request.asUser(DEFAULT_USER).to(tag.uri())
                .withField("name", "New name")
                .patch().isOk();

        request.asUser(DEFAULT_USER).to(tag.uri())
                .get().isOk()
                .hasField("$.name", isEqualTo("New name"))
                .hasField("$.updated_at", existsAndNotNull());

    }

    @Test
    public void post_success() {

        var tag = request.asUser(DEFAULT_USER).to("tags")
                .withField("name", "Tag A")
                .post().isCreated()
                .getLocation();

        request.asUser(DEFAULT_USER).to(tag)
                .get().isOk()
                .hasField("$.name", isEqualTo("Tag A"));

    }

    @Test
    public void get_success_list() {

        entityRequest.asUser(DEFAULT_USER).makeTags("Tag A", "Tag B", "Tag C");

        request.asUser(DEFAULT_USER).to("tags")
                .get().isOk()
                .hasField("$._embedded.tags.length()", isEqualTo(3))
                .hasField("$._embedded.tags[?(@.name == 'Tag A')]", exists())
                .hasField("$._embedded.tags[?(@.name == 'Tag B')]", exists())
                .hasField("$._embedded.tags[?(@.name == 'Tag C')]", exists())
                .hasField("$._embedded.tags[0]._links.self.href", contains("/tags/"))
                .hasField("$._embedded.tags[0]._links.all.href", contains("/tags"));

    }

    //endregion

}
