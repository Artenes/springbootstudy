# Spring Boot Playground

My personal BOK for things related to back-end engineering in a spring boot and java contexts.

Most examples present on ``docs`` folder are snippets from the code, so they will probably not make sense right away. 

Don't mind the mess! This is more of a playground to test and keep track of things I am learning.

# Index

## Request
- [Validation](docs/validation.md)
- [API versioning](docs/api-versioning.md)
- [Security](docs/security.md)

## Response
- [HATEOAS and HAL](docs/hateoas-hal.md)
- [Paging, Filtering, Sorting](docs/paging-filtering-sorting.md)
- [URI building](docs/uri-building.md)
- [Internationalization](docs/internationalization.md)
- [Exception Handling](docs/exception-handling.md)
- [CORS](docs/cors.md)
  https://github.com/spring-projects/spring-hateoas/issues/1306

## DevOps
- [Monitoring](docs/monitoring.md)
- [AWS Overview](docs/aws-overview.md)
- [Elastic Beanstalk](docs/elastic-beanstalk.md)
- [Docker](docs/docker-compose.md)
- [Kubernetes](docs/kubernetes.md)

## Others
- [Dealing with date and time](docs/dealing-date-time.md)
- [API design](docs/api-design.md)
- [Spring JPA](docs/spring-jpa.md)
https://reflectoring.io/spring-boot-feature-flags/

## Performance
- Load balancing test (Apache Jmeter)
- Cache at server side (Redis)
- Cache at client site (Cache-Control header)
- [Rate Limit](https://reflectoring.io/rate-limiting-with-resilience4j/)
- [Circuit Breaker](https://reflectoring.io/circuitbreaker-with-resilience4j/)
- [Bulkhead](https://reflectoring.io/bulkhead-with-resilience4j/)
- [Timeouts](https://reflectoring.io/time-limiting-with-resilience4j/)
- [Retry](https://reflectoring.io/retry-with-resilience4j/)
- load balance

## Monitoring

- logstash, kibana and elasticsearch
- logs configuration for production (identify logs by user uuid maybe?)
- health checks

# async processing

- long term execution with jobs
- Spring Async
- Spring Webflux

# security

- check about CORS

# tests

- other forms of tests that are not based on black-box

# notification channels

- message queues
- emails
- sms
- slack

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

## Load balance testing

- Apache JMeter - stress test to help find bottlenecks or to just stress the app to see how it will behave

## Postgres

``psql -U postgres``
``\l`` -  list all databases

## Timezone
- when saving dates in database, the datetime field will be normalized to UTC+0 by spring JPA
- when retrieving dates in database, the datetime field will be normalized to UTC+/- current JVM timezone
- so, when sending a response to the client, the datetime must be changed to the user's preferred timezone
- there might be a need to configure either mysql or postgres to use UTC by default as timezone when saving datetime information
- we use ISO 8601 to display the date back to user

## Cache header and cache engines

A cache header and a cache database like Redis serve different purposes and can be used together to optimize web application performance. Here are some scenarios where you might choose one over the other:

Use a cache header when:

You want to cache a static resource: If the resource is static (e.g., images, CSS, JavaScript files), you can use cache headers to instruct the browser to cache the resource. This can improve the page load time for subsequent requests.

You want to control the caching behavior for a specific resource: If you want to control the caching behavior for a specific resource, you can use cache headers to set the caching rules. For example, you can set the max-age directive to specify the maximum time a resource can be cached.

You want to minimize server load: If you have a high-traffic website and want to reduce the load on your servers, you can use cache headers to instruct the browser to cache resources. This can reduce the number of requests your server has to handle.

Use a cache database like Redis when:

You want to cache dynamic data: If your web application generates dynamic content that cannot be cached using headers, you can use a cache database like Redis to store the results of database queries or expensive computations. This can reduce the response time for subsequent requests.

You want to share cache data across multiple servers: If you have a distributed web application running on multiple servers, you can use a cache database like Redis to store the cache data. This ensures that all servers can access the same cache data, improving the cache hit rate.

You want more control over the cache eviction policy: If you want to control when the cache data is evicted, you can use a cache database like Redis to set the eviction policy. Redis supports various eviction policies, such as time-based eviction, LRU (least recently used) eviction, and LFU (least frequently used) eviction.

In summary, a cache header and a cache database like Redis can be used together to optimize web application performance. Use a cache header when you want to cache static resources or control the caching behavior for specific resources. Use a cache database like Redis when you want to cache dynamic data, share cache data across multiple servers, or have more control over the cache eviction policy.

# Questions

## Stress test with JMeter

Latency: The number of milliseconds that elapsed between when JMeter sent the request and when an initial response was received

Load Time: The number of milliseconds that the server took to fully serve the request (response + latency)

According to the View Results in Tree output, the load Time is 144. This is fairly a high value for a simple Rest API, but in my case, this endpoint does few heavy tasks and that is acceptable for me. You might be having different results than me based on what your REST API endpoint does and based on other factors, such as; geographical distance (which generally increases latency), the size of the requested item (which increases transfer time) etc.. So don’t worry about the result, if you have different values than mine.

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