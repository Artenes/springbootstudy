package degallant.github.io.todoapp.domain;

import degallant.github.io.todoapp.test.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.reactive.server.JsonPathAssertions;

import java.util.UUID;

class ProjectsTests extends IntegrationTest {

    @Test
    public void create_failsWithEmptyText() {

        request.asUser(DEFAULT_USER).to("projects")
                .withField("title", "")
                .post().isBadRequest()
                .hasField("$.errors[0].type", contains("validation.is_empty"));

    }

    @Test
    public void create_aProject() {

        var uri = request.asUser(DEFAULT_USER).to("projects")
                .withField("title", "A Project")
                .post().isCreated()
                .getLocation();

        request.asUser(DEFAULT_USER).to(uri)
                .get().isOk()
                .hasField("$.title", isEqualTo("A Project"));

    }

    @Test
    public void details_failsWithInvalidId() {

        request.asUser(DEFAULT_USER).to("projects/invalid")
                .get().isNotFound();

    }

    @Test
    public void details_failsWithUnknownId() {

        var commentId = UUID.randomUUID();
        request.asUser(DEFAULT_USER).to("projects/" + commentId)
                .get().isNotFound();

    }

    @Test
    public void details_showsProjectInfo() {

        var projectId = entityRequest.asUser(DEFAULT_USER).makeProject("A Project").uuid();
        request.asUser(DEFAULT_USER).to("projects/" + projectId)
                .get().isOk()
                .hasField("$.title", isEqualTo("A Project"));

    }

    @Test
    public void user_canOnlySeeItsProjects() {

        var projectUri = entityRequest.asUser("another@gmail.com").makeProject("A Project").uri();
        request.asUser(DEFAULT_USER).to(projectUri).get().isNotFound();

    }

    @Test
    public void list_noItems() {

        request.asUser(DEFAULT_USER).to("projects")
                .get()
                .hasField("$._embedded.projects.length()", isEqualTo(0));

    }

    @Test
    public void list_allProjects() {

        entityRequest.asUser(DEFAULT_USER).makeProjects("Project A", "Project B", "Project C");

        request.asUser(DEFAULT_USER).to("projects")
                .get()
                .hasField("$._embedded.projects.length()", isEqualTo(3))
                .hasField("$._embedded.projects[?(@.title == 'Project A')]", JsonPathAssertions::exists)
                .hasField("$._embedded.projects[?(@.title == 'Project B')]", JsonPathAssertions::exists)
                .hasField("$._embedded.projects[?(@.title == 'Project C')]", JsonPathAssertions::exists);

    }

}
