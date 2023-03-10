# Security

There are a lot of different concerns when regarding security. This page is to list some interesting links that can help
with discovery of problems that you already knew it can happen or new ones that you never heard of.

## Managing exceptions in filters

Usually we use filters to add some security checks in our request stack, but in spring boot the default exception handler
we register for controllers is not fired when an error happens within a filter. To deal with this we have to setup a base
filter that will handle all the exceptions and delegate them to our ``@ControllerAdvice`` instance. Or just add a try-catch
in your filter and delegate to the exception handler.

## Changing authentication process

In case you want to use JWT to authenticate your endpoints at ``/api/v1`` and use key-secret pair for any other endpoint
such as ``actuator`` you can do something like this:

````java
@Bean
    @Order(10)
    public SecurityFilterChain actuatorFilterChain(HttpSecurity http) throws Exception {

        http
                .securityMatcher("/actuator/**")
                .authorizeHttpRequests(authorize -> authorize
                        .anyRequest().authenticated()
                        .and().addFilterBefore(apiKeyFilter, UsernamePasswordAuthenticationFilter.class)
                )
                .csrf().disable()
                .cors();

        return http.build();

    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    
        http
                .csrf().disable()
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.POST, "/v1/auth").permitAll()
                        .requestMatchers(HttpMethod.POST, "/v1/auth/email").permitAll()
                        .requestMatchers(HttpMethod.POST, "/v1/admin/auth").permitAll()
                        .requestMatchers("/v1/admin/**").access(withRole(Role.ROLE_ADMIN))
                        .anyRequest().access(withRole(Role.ROLE_USER))
                        .and().addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                )
                .cors();

        return http.build();

    }
````

Note that we have defined a ``SecurityFilterChain`` for ``actuator`` endpoints and another for everything else. Unfortunately
they do not stack on top of each other, the first one to match a URL path will be the filter chain being use (aka you need to repeat ``csrf().disable()`` on all of them).



## Links

- [OWASP API Security Top 10](https://github.com/OWASP/API-Security)
- [How to Define a Spring Boot Filter?](https://www.baeldung.com/spring-boot-add-filter)
- [How to manage exceptions thrown in filters in Spring?](https://stackoverflow.com/questions/34595605/how-to-manage-exceptions-thrown-in-filters-in-spring)
- [How to apply Spring Security filter only on secured endpoints?](https://stackoverflow.com/questions/36795894/how-to-apply-spring-security-filter-only-on-secured-endpoints)
- [Spring Security](https://docs.spring.io/spring-security/reference/index.html)
- [Java Configuration](https://docs.spring.io/spring-security/reference/servlet/configuration/java.html)
- [Negate Request Matchers](https://stackoverflow.com/questions/42121778/how-to-allow-api-through-my-basic-auth-config-and-into-my-oauth-config-in)
- [Spring Security Authentication Provider](https://www.baeldung.com/spring-security-authentication-provider)
- [Spring Security: Authentication and Authorization In-Depth](https://www.marcobehler.com/guides/spring-security)