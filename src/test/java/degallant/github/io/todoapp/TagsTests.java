package degallant.github.io.todoapp;

import degallant.github.io.todoapp.test.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.reactive.server.JsonPathAssertions;

import java.util.UUID;

public class TagsTests extends IntegrationTest {

    @Test
    public void create_failsWithEmptyText() {

        request.asUser(DEFAULT_USER).to("tags")
                .withField("name", "")
                .post().isBadRequest()
                .hasField("$.errors[0].type", contains("validation.is_empty"));

    }

    @Test
    public void create_aTag() {

        var uri = request.asUser(DEFAULT_USER).to("tags")
                .withField("name", "A Tag")
                .post().isCreated()
                .getLocation();

        request.asUser(DEFAULT_USER).to(uri)
                .get().isOk()
                .hasField("$.name", isEqualTo("A Tag"));

    }

    @Test
    public void details_failsWithInvalidId() {

        request.asUser(DEFAULT_USER).to("tags/invalid")
                .get().isNotFound();

    }

    @Test
    public void details_failsWithUnknownId() {

        var commentId = UUID.randomUUID();
        request.asUser(DEFAULT_USER).to("tags/" + commentId)
                .get().isNotFound();

    }

    @Test
    public void details_showsTagInfo() {

        var projectId = entityRequest.asUser(DEFAULT_USER).makeTag("A Tag").uuid();
        request.asUser(DEFAULT_USER).to("tags/" + projectId)
                .get().isOk()
                .hasField("$.name", isEqualTo("A Tag"));

    }

    @Test
    public void user_canOnlySeeItsTags() {

        var tagUri = entityRequest.asUser("another@gmail.com").makeTag("A Tag").uri();
        request.asUser(DEFAULT_USER).to(tagUri).get().isNotFound();

    }

    @Test
    public void list_noItems() {

        request.asUser(DEFAULT_USER).to("tags")
                .get()
                .hasField("$._embedded.tags.length()", isEqualTo(0));

    }

    @Test
    public void list_allTags() {

        entityRequest.asUser(DEFAULT_USER).makeTags("Tag A", "Tag B", "Tag C");

        request.asUser(DEFAULT_USER).to("tags")
                .get()
                .hasField("$._embedded.tags.length()", isEqualTo(3))
                .hasField("$._embedded.tags[?(@.name == 'Tag A')]", JsonPathAssertions::exists)
                .hasField("$._embedded.tags[?(@.name == 'Tag B')]", JsonPathAssertions::exists)
                .hasField("$._embedded.tags[?(@.name == 'Tag C')]", JsonPathAssertions::exists);

    }

}
