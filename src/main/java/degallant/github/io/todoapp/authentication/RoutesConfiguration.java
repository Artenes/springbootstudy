package degallant.github.io.todoapp.authentication;

import degallant.github.io.todoapp.users.Role;
import lombok.RequiredArgsConstructor;
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

import java.util.Arrays;

/**
 * @noinspection ClassCanBeRecord, unused, SameParameterValue
 */
@EnableWebSecurity
@Configuration
@RequiredArgsConstructor
public class RoutesConfiguration {

    private final JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf().disable()
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.POST, "/v1/auth").permitAll()
                        .requestMatchers(HttpMethod.GET, "/v1/auth/refresh").authenticated()
                        .anyRequest().access(withRole(Role.ROLE_USER))
                        .and().addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                );

        return http.build();

    }

    private AuthorityAuthorizationManager<RequestAuthorizationContext> withRole(Role role) {
        var access = AuthorityAuthorizationManager.<RequestAuthorizationContext>hasRole(role.simpleName());
        var hierarchy = new RoleHierarchyImpl();
        hierarchy.setHierarchy("ROLE_ADMIN > ROLE_USER");
        access.setRoleHierarchy(hierarchy);
        return access;
    }

}
