package degallant.github.io.todoapp;

import com.fasterxml.jackson.databind.ObjectMapper;
import degallant.github.io.todoapp.authentication.ApiKeyRepository;
import degallant.github.io.todoapp.authentication.AuthenticationService;
import degallant.github.io.todoapp.authentication.CorsAppConfiguration;
import degallant.github.io.todoapp.authentication.JwtFilter;
import degallant.github.io.todoapp.domain.users.Role;
import degallant.github.io.todoapp.exceptions.AppExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authorization.AuthorityAuthorizationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * @noinspection unused, SameParameterValue
 */
@EnableWebSecurity
@Configuration
public class RoutesConfiguration {

    private final JwtFilter jwtFilter;
    private final RoleHierarchyImpl roleHierarchy;
    private final CorsAppConfiguration corsAppConfiguration;

    public RoutesConfiguration(
            CorsAppConfiguration corsAppConfiguration,
            AuthenticationService authenticationService,
            AppExceptionHandler appExceptionHandler,
            ObjectMapper objectMapper
    ) {
        this.jwtFilter = new JwtFilter(authenticationService, appExceptionHandler, objectMapper);
        this.corsAppConfiguration = corsAppConfiguration;
        roleHierarchy = new RoleHierarchyImpl();
        roleHierarchy.setHierarchy("ROLE_ADMIN > ROLE_USER");
    }

    @Bean
    @Order(10)
    public SecurityFilterChain actuatorFilterChain(HttpSecurity http, ApiKeyRepository apiKeyRepository) throws Exception {

        http
                .securityMatcher("/actuator/**")
                .authorizeHttpRequests(authorize -> authorize.anyRequest().authenticated())
                .csrf().disable()
                .cors()
                .and().httpBasic()
                .and().userDetailsService(new ApiKeyDetailsService(apiKeyRepository));

        return http.build();

    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        //TODO format response when user does not have access
        //TODO generalize matches so version is not take into account

        http
                .csrf().disable()
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.POST, "/v*/auth").permitAll()
                        .requestMatchers(HttpMethod.POST, "/v*/auth/email").permitAll()
                        .requestMatchers(HttpMethod.POST, "/v*/admin/auth").permitAll()
                        .requestMatchers("/v*/admin/**").access(withRole(Role.ROLE_ADMIN))
                        .anyRequest().access(withRole(Role.ROLE_USER))
                        .and().addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                )
                .cors();

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

    private AuthorityAuthorizationManager<RequestAuthorizationContext> withRole(Role role) {
        var access = AuthorityAuthorizationManager.<RequestAuthorizationContext>hasRole(role.simpleName());
        access.setRoleHierarchy(roleHierarchy);
        return access;
    }

}
