# Uri Building

Add this to your dependencies:

````groovy
implementation 'org.springframework.boot:spring-boot-starter-hateoas'
````

Import these in your class:

````java
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
````

And generate the URIs in your method:

````java
Link selfLink = linkTo(methodOn(getClass()).profile(authentication)).withSelfRel();
Link tasksLink = linkTo(methodOn(TasksController.class).list(null, null, null, null, null, authentication)).withRel("tasks");
Link tagsLink = linkTo(methodOn(TagsController.class).list(authentication)).withRel("tags");
Link projectsLink = linkTo(methodOn(ProjectsController.class).list(authentication)).withRel("projects");
````

You can checkout the [HATEOAS library tests](https://github.com/spring-projects/spring-hateoas/blob/0.23.0.RELEASE/src/test/java/org/springframework/hateoas/mvc/ControllerLinkBuilderUnitTest.java#L57) for some more examples of usage.

## Limitations

The HATEOAS API for generating URIs is not flexible. You will face problems specially when generating links with query parameters, since you can't control which ones are displayed.

[There is a workaround](https://github.com/spring-projects/spring-hateoas/issues/535) (that does not work anymore), so the solution is to generate the link, then manually manipulate it to remove what you want or not. 

Because of this edge case and other ones that I am yet to face, I rather manually generate the URIs to have more control of what is displayed at a given time.

You can get the base URL of the application with this method (no, spring boot does not provide any simple way to just get the base url of the application):

````java
private String makeBaseUrl() {
    RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
    if (!(requestAttributes instanceof ServletRequestAttributes)) {
        throw new RuntimeException("Was not possible to get base url");
    }
    HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
    return String.format("%s://%s:%d", request.getScheme(), request.getServerName(), request.getServerPort());
}
````

Then you append the path to your route:

````java
var root = makeBaseUrl();
var url = Link.of(root + "/v1/tasks").withSelfRel();
````