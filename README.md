# TO DO

# stress tests

- see how to do stress test on spring boot app
- check about caching - Cache-Control header
- deal with rate limit
- load balance

# security

- check about CORS

# log and monitoring

- logstash, kibana and elasticsearch
- logs configuration for production (identify logs by user uuid maybe?)

# notification channels

- message queues
- emails
- sms
- slack/discord

# others

- add support to check style

- practice regex?

- check about docker

- improve admin endpoint and change authentication to password
  - manage users
  - manage admins
  - different auth process
  - manage api keys
  - allow creation of access keys for tests

- create empty sample project based on this one

- deploy application to AWS

- write about everything

- check about graphql

- get back to OpenAPI/Swagger

# Explanations

## Timezone
- when saving dates in database, the datetime field will be normalized to UTC+0 by spring JPA
- when retrieving dates in database, the datetime field will be normalized to UTC+/- current JVM timezone
- so, when sending a response to the client, the datetime must be changed to the user's preferred timezone
- there might be a need to configure either mysql or postgres to use UTC by default as timezone when saving datetime information
- we use ISO 8601 to display the date back to user

# Questions

## How to manage admin routes and other sensitive data?

The best way to handle this is to create another application. One would be the public API for your first-party clients, 
while the other will have access to the same database as the first one, but its sole purpose is to manage users and API keys.
The advantage of putting this on another application is the possibility of restricting its access with a more tight authentication
process (e.g. 2FA) and limiting its access by IP address so only some people can use it. 

# Resources test cases

- **get (resource)**
    - id is not a valid UUID
    - id is UUID but does not exist
    - id is of resource that belongs to another user
    - id is of resource that was deleted
    - show dates according to offset in header
    
    
- **patch (resource)**
    - id is not a valid UUID
    - id is UUID but does not exist
    - id is of resource that belongs to another user
    - id is of resource that was deleted
    - request body is invalid
    - returns status "no content" if resource was not updated 
    - show translated error messages
      

- **delete (resource)**
    - id is not a valid UUID
    - id is UUID but does not exist
    - id is of resource that belongs to another user
    - id is of resource that was deleted


- **post (new resource)**
    - request body is invalid
    - show translated error messages
    

- **get (resource list)**
    - shows no items
    - ignore deleted items
    - show dates according to offset in header
    - ignore resources that belong to other users

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