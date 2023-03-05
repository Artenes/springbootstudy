package degallant.github.io.todoapp;

import degallant.github.io.todoapp.authentication.CorsAppConfiguration;
import degallant.github.io.todoapp.authentication.JwtFilter;
import degallant.github.io.todoapp.domain.users.Role;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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

    public RoutesConfiguration(JwtFilter jwtFilter, CorsAppConfiguration corsAppConfiguration) {
        this.jwtFilter = jwtFilter;
        this.corsAppConfiguration = corsAppConfiguration;
        roleHierarchy = new RoleHierarchyImpl();
        roleHierarchy.setHierarchy("ROLE_ADMIN > ROLE_USER");
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        //TODO format response when user does not have access
        //TODO generalize matches so version is not take into account

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
