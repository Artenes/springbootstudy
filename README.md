# TO DO

- make refresh token works

- add test for invalid tokens

- extract authenticate user for easier access

- deal with users roles

- check about caching - Cache-Control header

- deal with rate limit

- deal with logging and monitoring

- check about graphql

- get back to OpenAPI/Swagger

- check about CORS

- check about relationships in hibernate for lazy loading relationships

- add support to check style

- fix timestamp handling, needs to save as +0 and retrieve in -4

- see how to do stress test on spring boot app

- practice regex?

# References

- rest api design https://stackoverflow.blog/2020/03/02/best-practices-for-rest-api-design/

- about date time in java https://reflectoring.io/spring-timezones/

- retardedly, you can't have enums in the database and bring them to hibernate, maybe this can help: https://docs.jboss.org/hibernate/orm/6.1/userguide/html_single/Hibernate_User_Guide.html#basic-enums

- when creating queries in a repository, by default it uses JPQL

- top 10 security issues with rest apis https://github.com/OWASP/API-Security

- api versioning https://www.troyhunt.com/your-api-versioning-is-wrong-which-is/

- HATEOAS https://stateless.co/hal_specification.html https://restfulapi.net/hateoas/

- API paging https://www.mixmax.com/engineering/api-paging-built-the-right-way

- Error response standards https://www.rfc-editor.org/rfc/rfc7807.html

- Exception handling tutorial https://reflectoring.io/spring-boot-exception-handling/

- Internationalization tutorial https://reflectoring.io/spring-boot-internationalization/

- Validation tutorial https://reflectoring.io/bean-validation-with-spring-boot

- Removing query args from url https://github.com/spring-projects/spring-hateoas/issues/535

- Examples on how to use uri building https://github.com/spring-projects/spring-hateoas/blob/0.23.0.RELEASE/src/test/java/org/springframework/hateoas/mvc/ControllerLinkBuilderUnitTest.java#L57

- HAL pagination https://apigility.org/documentation/api-primer/halprimer