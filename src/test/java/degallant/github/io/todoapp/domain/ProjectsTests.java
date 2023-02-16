package degallant.github.io.todoapp.domain;

import degallant.github.io.todoapp.test.IntegrationTest;
import org.junit.jupiter.api.Test;

import java.util.UUID;

class ProjectsTests extends IntegrationTest {

    @Test
    public void get_failsWhenProjectIsInvalid() {

        var projectId = entityRequest.asUser(DEFAULT_USER).makeProject("Project A");
        var anotherId = entityRequest.asUser(ANOTHER_USER).makeProject("Project X");

        request.asUser(DEFAULT_USER).to("projects/invalid").get().isNotFound();
        request.asUser(DEFAULT_USER).to("projects/" + UUID.randomUUID()).get().isNotFound();
        request.asUser(DEFAULT_USER).to(anotherId.uri()).get().isNotFound();

        request.asUser(DEFAULT_USER).to(projectId.uri()).delete().isNoContent();
        request.asUser(DEFAULT_USER).to(projectId.uri()).get().isNotFound();

    }

    @Test
    public void patch_failsWhenProjectIsInvalid() {

        var projectId = entityRequest.asUser(DEFAULT_USER).makeProject("Project A");
        var anotherId = entityRequest.asUser(ANOTHER_USER).makeProject("Project X");

        request.asUser(DEFAULT_USER).to("projects/invalid").withField("title", "some").patch().isNotFound();
        request.asUser(DEFAULT_USER).to("projects/" + UUID.randomUUID()).withField("title", "some").patch().isNotFound();
        request.asUser(DEFAULT_USER).to(anotherId.uri()).withField("title", "some").patch().isNotFound();

        request.asUser(DEFAULT_USER).to(projectId.uri()).delete().isNoContent();
        request.asUser(DEFAULT_USER).to(projectId.uri()).withField("title", "some").patch().isNotFound();

    }

    @Test
    public void delete_failsWhenProjectIsInvalid() {

        var projectId = entityRequest.asUser(DEFAULT_USER).makeProject("Project A");
        var anotherId = entityRequest.asUser(ANOTHER_USER).makeProject("Project X");

        request.asUser(DEFAULT_USER).to("projects/invalid").delete().isNotFound();
        request.asUser(DEFAULT_USER).to("projects/" + UUID.randomUUID()).delete().isNotFound();
        request.asUser(DEFAULT_USER).to(anotherId.uri()).delete().isNotFound();

        request.asUser(DEFAULT_USER).to(projectId.uri()).delete().isNoContent();
        request.asUser(DEFAULT_USER).to(projectId.uri()).delete().isNotFound();

    }

    @Test
    public void post_failsWithInvalidBody() {

        request.asUser(DEFAULT_USER).to("projects").withField("title", "")
                .post().isBadRequest().hasField("$.errors[0].type", contains("validation.is_empty"));

        request.asUser(DEFAULT_USER).to("projects").withField("random", "")
                .post().isBadRequest().hasField("$.errors[0].type", contains("validation.is_required"));

    }

    @Test
    public void patch_failsWithInvalidBody() {

        var projectId = entityRequest.asUser(DEFAULT_USER).makeProject("Project A");

        request.asUser(DEFAULT_USER).to(projectId.uri()).withField("title", "")
                .patch().isBadRequest().hasField("$.errors[0].type", contains("validation.is_empty"));

    }

    @Test
    public void get_listIgnoresDeletedProjects() {

        var projects = entityRequest.asUser(DEFAULT_USER).makeProjects("Project A", "Project B", "Project C");

        request.asUser(DEFAULT_USER).to(projects.get(1).uri()).delete().isNoContent();

        request.asUser(DEFAULT_USER).to("projects").get().isOk()
                .hasField("$._embedded.projects.length()", isEqualTo(2))
                .hasField("$._embedded.projects[?(@.title == 'Project A')]", exists())
                .hasField("$._embedded.projects[?(@.title == 'Project C')]", exists());

    }

    @Test
    public void get_listShowOnlyProjectsCreatedByUser() {

        entityRequest.asUser(DEFAULT_USER).makeProjects("Project A", "Project B", "Project C");
        entityRequest.asUser(ANOTHER_USER).makeProjects("Project X, Project Y");

        request.asUser(DEFAULT_USER).to("projects").get().isOk()
                .hasField("$._embedded.projects.length()", isEqualTo(3))
                .hasField("$._embedded.projects[?(@.title == 'Project A')]", exists())
                .hasField("$._embedded.projects[?(@.title == 'Project B')]", exists())
                .hasField("$._embedded.projects[?(@.title == 'Project C')]", exists());

    }

    @Test
    public void get_listShowNoItems() {

        request.asUser(DEFAULT_USER).to("projects")
                .get().hasField("$._embedded.projects.length()", isEqualTo(0));

    }

    @Test
    public void post_createsAProject() {

        var uri = request.asUser(DEFAULT_USER).to("projects")
                .withField("title", "A Project")
                .post().isCreated()
                .getLocation();

        request.asUser(DEFAULT_USER).to(uri)
                .get().isOk()
                .hasField("$.title", isEqualTo("A Project"));

    }

    @Test
    public void get_showsProjectDetails() {

        var project = entityRequest.asUser(DEFAULT_USER).makeProject("Project A");

        request.asUser(DEFAULT_USER).to(project.uri())
                .get()
                .hasField("$.title", isEqualTo("Project A"))
                .hasField("$.created_at", existsAndNotNull())
                .hasField("$.updated_at", exists())
                .hasField("$._links.self", existsAndNotNull())
                .hasField("$._links.all", existsAndNotNull());

    }

    @Test
    public void get_showsProjectDetailsInList() {

        entityRequest.asUser(DEFAULT_USER).makeProjects("Project A", "Project B");

        request.asUser(DEFAULT_USER).to("projects")
                .get()
                .hasField("$._embedded.projects.length()", isEqualTo(2))
                .hasField("$._embedded.projects[0].title", isEqualTo("Project A"))
                .hasField("$._embedded.projects[0].created_at", existsAndNotNull())
                .hasField("$._embedded.projects[0].updated_at", exists())
                .hasField("$._embedded.projects[0]._links.self", existsAndNotNull())
                .hasField("$._embedded.projects[0]._links.all", existsAndNotNull())
                .hasField("$._embedded.projects[1].title", isEqualTo("Project B"));

    }

    @Test
    public void patch_updatesProject() {

        var project = entityRequest.asUser(DEFAULT_USER).makeProject("Project A");

        request.asUser(DEFAULT_USER).to(project.uri()).withField("title", "New title").patch().isOk();

        request.asUser(DEFAULT_USER).to(project.uri()).get().isOk()
                .hasField("$.title", isEqualTo("New title"));

    }

    @Test
    public void patch_doesNotUpdatesProject() {

        var project = entityRequest.asUser(DEFAULT_USER).makeProject("Project A");

        request.asUser(DEFAULT_USER).to(project.uri()).withField("random_field", "invalid").patch().isNoContent();

    }

    //TODO if user is deleted?

}
