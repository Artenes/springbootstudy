package degallant.github.io.todoapp.authentication;

import degallant.github.io.todoapp.users.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.Arrays;

/**
 * @noinspection ClassCanBeRecord, unused
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
                        .anyRequest().hasAnyRole(roles())
                        .and().addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                );

        return http.build();

    }

    private String[] roles() {
        return Arrays.stream(Role.values()).map(Role::simpleName).toArray(String[]::new);
    }

}
