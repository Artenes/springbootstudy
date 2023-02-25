# Validation

Add this dependency:

````groovy
implementation 'org.springframework.boot:spring-boot-starter-validation'
````

## Validating request body

Add the validation annotations into the fields of the class the models the body of the request

````java
public class Task {
    
    @NotBlank
    String name;
    
    @NotNull
    Boolean complete;
    
}
````

And in the controller's method add the ``@Valid`` annotation:

````java
public ResponseEntity<?> create(@RequestBody @Valid Task request)
````

## Validating url params

Add the ``@Validated`` annotation to your controller class.

````java
@RestController
@Validated
public class TasksController
````

And in the method add the validation rule to the argument (in this case is the ``@Positive`` annotation):

````java
@GetMapping
public ResponseEntity<?> list(@Positive @RequestParam(name = "p", defaultValue = "1") int page)
````

## Limitations

This validation mechanism is not that flexible because it tight everything together using annotations, which makes harder
to add custom logic during validation. You can create custom validation annotations, but again, you are isolated to validate only that field.

Sometimes might be necessary to validate a field based on another field and most of the time, the act of parsing a field
is a validation on its own, generating this pattern where some fields are validated using this mechanism and others are not
because they are being parsed within the controller's method.

Also they do not provide support to change the name of the field to an arbitrary value. If you want a field called ``p``, 
but when working in code you wish it was called ``page``, you can't just do that by using annotations.

There is also another case where if you expect a non-String type in the body response or url argument and the client sends
something invalid, it will cause Spring Boot to throw an exception that differs from the one thrown when a validation fails.
This causes the problem that when the client sends invalid data, two possible outcomes are possible, instead of just a 400
response showing the fields with errors.

For this lack of flexibility I prefer to not use this validation mechanism and validate all fields in the controller's method
so I can have proper control over how the validation/parsing happens and what is the exception thrown when an error happens.

Refer to the [sanitization package](../src/main/java/degallant/github/io/todoapp/sanitization) in the project to check how I implemented a solution that brings together validation and parsing
as a means to sanitize data that the client sends. The main class of the package is the ``Sanitizer`` class.

Here is a demonstration of it:

````java
var result = sanitizer.sanitize(
    sanitizer.field("name").withRequiredValue(request.getName()).sanitize(value -> {
        rules.isNotEmpty(value);
        return value; //maybe do a parsing/transformation before returning the value 
    })
);
````

The idea is that the lambda will throw any exception if there is a validation error and return a parsed/sanitized value for that field.
The final result is an object that contains all sanitized fields, which you can query any of them afterwards.

## Links

- [Validation with Spring Boot - the Complete Guide](https://reflectoring.io/bean-validation-with-spring-boot)