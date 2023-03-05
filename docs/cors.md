# CORS

Add to the ``application.properties`` file custom keys to hold CORS configuration:

````
app.cors.allowed_origins=https://www.test-cors.org
app.cors.allowed_methods=GET,POST,PATCH,PUT,DELETE,OPTIONS,HEAD
app.cors.max_age=3600
app.cors.allowed_headers=Requestor-Type
app.cors.exposed_headers=X-Get-Header
````

Create a class to receive this configuration information:

````java
@ConfigurationProperties("app.cors")
public record CorsAppConfiguration(
        String[] allowedOrigins,
        String[] allowedMethods,
        long maxAge,
        String[] allowedHeaders,
        String[] exposedHeaders
) {
}
````

Then setup the CORS configuration alongside the security configuration:

````java
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.csrf().disable().cors();
    return http.build();
}

@Bean
public CorsConfigurationSource corsConfigurationSource() {
    var configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(Arrays.asList(corsAppConfiguration.allowedOrigins()));
    configuration.setAllowedMethods(Arrays.asList(corsAppConfiguration.allowedMethods()));
    configuration.setAllowedHeaders(Arrays.asList(corsAppConfiguration.allowedHeaders()));
    configuration.setExposedHeaders(Arrays.asList(corsAppConfiguration.exposedHeaders()));
    configuration.setMaxAge(corsAppConfiguration.maxAge());

    var source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);

    return source;
}
````

If you don't setup CORS, no browser client will be able to make requests to your endpoints.