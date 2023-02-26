# Dealing with date and time

In a spring boot app you will always be dealing with date and time by using the ``OffsetDateTime`` class when working with 
objects and the ``ISO-8601 `` standard when working with strings. 

So, whenever the client sends a date time string to the back-end or the back-end sends a date time string to the client, 
this has to be in ``ISO-8601`` format, which looks like this: ``2007-12-03T10:15:30+01:00``.

Once the back-end received the date and time, it should be parsed to an ``OffsetDateTime`` instance for manipulation and storage.

## What to store in the database

In all the entities that uses date time fields, make them use ``OffsetDateTime``.

````java
@Entity
public Task {
    
    private OffsetDateTime dueDate;
    
}
````

When saving the data, spring boot JPA will save the date time field as UTC+0 automatically and when retrieving it from database
it will add the current's JVM default time zone to it.

## How to change the time zone for a user

Change the time zone by allowing the client to send the time zone it wants by a header in the request, something like this:

````
Accept-Offset: +04:00
````

The `Accept-Offset` is an arbitrary name for the header. Then when sending any response back to the client that has a date time on it, format it based on that offset provided by the user:

````java
var offset = request.getHeader("Accept-Offset");
var zoneOffset = ZoneOffset.of(offset);
var formatted = task.getDueDate().withOffsetSameInstant(zoneOffset);
````

You can do this manually on every request, or maybe use a ``HandlerInterceptor`` to format the response as its being returned
to the client. The latter will require some more work because you need to come up with a solution generic enough to deal with
any kind of response you generate. Unfortunately spring boot does not offer any mechanism to solve this problem.

## Links

- [Handling Timezones in a Spring Boot Application](https://reflectoring.io/spring-timezones/)