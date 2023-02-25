# HATEOAS and HAL

Add this to your dependencies:

````groovy
implementation 'org.springframework.boot:spring-boot-starter-hateoas'
````

And in your controller do the following to return a single resource:

````java
@GetMapping("/{id}")
public ResponseEntity<?> details(@PathVariable String id, Authentication authentication) {

    var user = (UserEntity) authentication.getPrincipal();
    var entity = projectsParser.toProjectOrThrowNoSuchElement(id, user);

    var project = ProjectsDto.Details.builder().id(entity.getId()).title(entity.getTitle()).build();
    var linkSelf = link.to("projects").slash(entity.getId()).withSelfRel();
    var linkAll = link.to("projects").withRel("all");
    var response = EntityModel.of(project).add(linkSelf, linkAll););

    return ResponseEntity.ok(response);
}
````

And like this for a collection:

````java
@GetMapping
public ResponseEntity<?> list(Authentication authentication) {

    var user = (UserEntity) authentication.getPrincipal();

    var projects = repository.findByUserIdAndDeletedAtIsNull(user.getId())
            .stream()
            .map(this::toEntityModel)
            .collect(Collectors.toList());

    var linkSelf = link.to("projects").withSelfRel();

    var response = HalModelBuilder.emptyHalModel()
            .embed(projects, ProjectsDto.Details.class)
            .link(linkSelf).build();

    return ResponseEntity.ok(response);
}
````

The general idea is to format the response following the [HAL specification](https://datatracker.ietf.org/doc/html/draft-kelly-json-hal). The snippet below shows how a HAL response looks like:

````json
{
    "_links": {
        "self": { "href": "/orders" },
        "curies": [{ "name": "ea", "href": "http://example.com/docs/rels/{rel}", "templated": true }],
        "next": { "href": "/orders?page=2" },
        "ea:find": {
            "href": "/orders{?id}",
            "templated": true
        },
        "ea:admin": [{
            "href": "/admins/2",
            "title": "Fred"
        }, {
            "href": "/admins/5",
            "title": "Kate"
        }]
    },
    "currentlyProcessing": 14,
    "shippedToday": 20,
    "_embedded": {
        "ea:order": [{
            "_links": {
                "self": { "href": "/orders/123" },
                "ea:basket": { "href": "/baskets/98712" },
                "ea:customer": { "href": "/customers/7809" }
            },
            "total": 30.00,
            "currency": "USD",
            "status": "shipped"
        }, {
            "_links": {
                "self": { "href": "/orders/124" },
                "ea:basket": { "href": "/baskets/97213" },
                "ea:customer": { "href": "/customers/12369" }
            },
            "total": 20.00,
            "currency": "USD",
            "status": "processing"
        }]
    }
}
````

## Links
- [HATEOAS Driven REST APIs](https://restfulapi.net/hateoas/)
- [HAL - Hypertext Application Language](https://stateless.co/hal_specification.html)
- [Hypertext Application Language (HAL) - Collections](https://apigility.org/documentation/api-primer/halprimer.html#)