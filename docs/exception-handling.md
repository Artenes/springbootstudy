# Exception Handling

Create a class with the following annotation:

````java
@ControllerAdvice
public class AppExceptionHandler
````

Add a method to handle an exception type:

````java
@ExceptionHandler(NoSuchElementException.class)
public ErrorResponse handleNoSuchElementException(NoSuchElementException exception)
````

Return an ``ErrorResponse`` instance with the information related to the problem:

````java
var builder = ErrorResponse.builder(new Exception(), HttpStatus.NOT_FOUND, "Requested element was not found");
builder.title("Not found");
builder.type(URI.create("http://app.com/error/not_found"));
builder.property("fieldA", "value");
return builder.build();
````

Returning an ``ErrorResponse`` is a way to generate [standardized response](https://www.rfc-editor.org/rfc/rfc7807.html) to the client when something goes wrong.

The answer will look something like this:

````json
{
    "type": "http://app.com/error/not_found",
    "title": "Not found",
    "detail": "Requested element was not found",
    "instance": "/api/v1/tasks",
    "fieldA": "value"
}
````

- The ``type`` field is just an arbitrary URI that you define to uniquely identify that error type.
- The ``instance`` is automatically filled by spring boot with the current requested path
- The ``fieldA`` is just an arbitrary field you can append to the response to add some extra data into the error

The support for this standard error format is only available for spring boot version 3 onwards. If you are using a previous
version you need to build this response yourself.

## Handling validation errors

You are going to follow the same steps above, but will handle the exception that is thrown when a validation error happens.
This will depend on how you are implementing your validation logic. The goal is to have an ``ErrorResponse`` instance
that has all the errors:

````json
"errors": [
    {
        "field": "title",
        "type": "https://todoapp.com/validation.is_required",
        "message": "Field title is required",
        "origin": "body"
    }
],
````

The format of each error is arbitrary, in this case I defined that I want to know the ``origin`` (either from url, header or body)
and the ``type`` as an URI.

## Handling other types of errors

Create a generic exception class that you can throw from any part of the code when something goes wrong and in your class annotated
with ``@ControllerAdvice``, handle that exception and return the corresponding ``ErrorResponse`` instance. This will avoid
the need to create multiple exception classes for each different error.

To have a unique URI for each error, you can just use a string to construct the Exception and pass a name for it and use it
to generate the URI to put in the ``type`` field in the ``ErrorResponse`` instance.

## Other types to handle

There are some common exceptions that you want to handle to have control over how the response is created:

- ``HttpMessageNotReadableException`` - when the request is not processable
- ``HttpRequestMethodNotSupportedException`` - when the request has an invalid type
- ``Exception`` - to capture any other kind of exception to not let random errors generate unwanted responses

## Links

- [Complete Guide to Exception Handling in Spring Boot](https://reflectoring.io/spring-boot-exception-handling/)
- [Problem Details for HTTP APIs](https://www.rfc-editor.org/rfc/rfc7807.html)