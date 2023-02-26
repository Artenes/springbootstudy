# API Versioning

Spring boot does not offer any tool to help with this. You will have to come up with a strategy to "version" your API.

The most common thing to do is to always avoid making changes that change the response the API creates. It can add new fields
to it, but never remove or change the name, type or structure of a field.

But when you have to make breaking changes, then you need to version the endpoint. For that we just put a "vX" prefix in the endpoint.

````
/v2/tasks
````

Now this endpoint has a v2 version. Note that you also need to leave the previous version available for use. Therefore in the
controller you will have two methods that do the same thing with some minor difference:

````java
@GetMapping("/v1/tasks")
public ResponseEntity<?> listV1()

@GetMapping("/v2/tasks")
public ResponseEntity<?> listV2()
````

This now creates the problem that you need to keep support for both endpoints. There is no solution for that, maybe you can
share code between them, but in the end there are two entry points to the same functionality that you need to keep in check.

## Links

- [Your API versioning is wrong, which is why I decided to do it 3 different wrong ways](https://www.troyhunt.com/your-api-versioning-is-wrong-which-is/)