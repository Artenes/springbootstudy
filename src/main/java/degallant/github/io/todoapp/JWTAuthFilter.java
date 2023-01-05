package degallant.github.io.todoapp;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JWTAuthFilter extends GenericFilter {

    private final AuthenticationService service;

    public JWTAuthFilter(AuthenticationService service) {
        this.service = service;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        String authHeader = ((HttpServletRequest) request).getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && !authHeader.isBlank() && authHeader.startsWith("Bearer ")) {
            String token = authHeader.split(" ")[1];
            Authentication authentication = service.authenticateWithJWTToken(token);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        chain.doFilter(request, response);
    }

}
