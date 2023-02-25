# Paging, Filtering, Sorting

This is something that you need to implement on your own on evey endpoint you need it. Maybe try to isolate 
the logic in a new class and reuse across multiple controllers. Spring boot does not have an API or standard to help with this.

Note that you can mix and match all these constraints when doing they query against the database, by passing these constraints
as arguments to the query method (e.g. passing to ``findAll`` method in a repository)

## Paging

In the controller method expect an argument for the page:

````java
@GetMapping
public ResponseEntity<?> list(@RequestParam(name = "p", defaultValue = "1") String page)
````

Make a ``PageRequest`` instance with it:

````java
var pageRequest = PageRequest.of(page - 1, 10); //the page index starts with 0
````

Then make a query in the database using it:

````java
var tasksPage = tasksRepository.findAll(pageRequest);
````

## Filtering

In the controller method expect an argument to use for filtering:

````java
@GetMapping
public ResponseEntity<?> list(@RequestParam(required = false) String title)
````

Creates an implementation of the ``Specification`` interface:

````java
public Specification<TaskEntity> matchesAnyOf(String title) {
    return (root, query, builder) -> {

        List<Predicate> predicates = new ArrayList<>();

        if (title != null && !title.isEmpty()) {
            //title is the name of the field in the entity class, not the column name
            predicates.add(builder.like(root.get("title"), "%" + title + "%"));
        }

        return builder.and(predicates.toArray(new Predicate[]{}));

    };
}
````

Then make a query in the database using it:

````java
var specification = matchesAnyOf(title);
var tasks = tasksRepository.findAll(specification);
````

Also make sure that your repository extends the ``JpaSpecificationExecutor`` interface.

The idea here is that by using ``Specification``s instances, we can dynamically create queries, so you can add more fields
in the url param to query more fields in the database.

## Sorting

In the controller method expect an argument to use for sorting:

````java
@GetMapping
public ResponseEntity<?> list(@RequestParam(name = "s", required = false) String sort)
````

Now you have to choose how to parse the value within this sort argument. Let's assume that our specification for sorting is:

````
http://localhost:8080/tasks?s=due_date:desc,title:asc
````

Where I have the name of the field I want to sort, followed by the sort direction.

After parsing this data, you must be able to do the following using the Sorting API from Spring JPA:

````java
var sorting = Sort.by("title").ascending().and(Sort.by("dueDate").descending())
````

Then make a query in the database using it:

````java
var tasks = tasksRepository.findAll(sorting);
````

You can also pass the sorting information to a ``PageRequest`` instance (which is the usual use case):

````java
var pageRequest = PageRequest.of(page - 1, 10, sorting);
````

To then make the query using the ``pageRequest`` object.

## Links

- [API Paging Built The Right Way](https://www.mixmax.com/engineering/api-paging-built-the-right-way)
- [Best practices for REST API design](https://stackoverflow.blog/2020/03/02/best-practices-for-rest-api-design/)