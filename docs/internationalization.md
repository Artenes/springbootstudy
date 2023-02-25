# Internationalization

Add this config to ``application.properties``:

````
spring.messages.basename=language/messages
````

Create the ``messages.properties`` file under ``src/main/resources/language`` with any set of text you need:

````
valiation.error.x=Field {0} is invalid because {1}
````

Note that the text identifiers can be anything.

In your class get an instance of ``MessageSource`` and call its ``getMessage`` method:

````java
@AutoWired
private MessageSource messageSource;

public String message() {
    var locale = LocaleContextHolder.getLocale();
    return messageSource.getMessage("validation.error.x", new String[]{ "Field", "Invalid" }, locale);
}
````

When calling the ``getMessage`` method you inform the id of the message, alongside any argument that will replace the placeholders
in the message body.

## Changing the language based on a header 

In the ``src/main/resources/language`` folder, create a new ``messages.properties`` file with a sufix with the region it belongs to
such as ``messages_pt_BR.properties``. In there put the translated strings from the default messages file.

In either the controller or in a ``HandlerInterceptor`` instance, set the current locale with a call to ``LocaleContextHolder::setLocale``:

````java
String locale = request.getHeader(HttpHeaders.ACCEPT_LANGUAGE);
LocaleContextHolder.setLocale(locale);
````

Then when we call ``MessageSource::getMessage``, it will use the new locale that will search in the `languages` folder for
a messages file corresponding to that locale.

## Links

[How to Internationalize a Spring Boot Application](https://reflectoring.io/spring-boot-internationalization/)