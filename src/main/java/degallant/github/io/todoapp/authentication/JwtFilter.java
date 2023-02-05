package degallant.github.io.todoapp.authentication;

import com.fasterxml.jackson.databind.ObjectMapper;
import degallant.github.io.todoapp.exceptions.AppExceptionHandler;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.http.entity.ContentType;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtFilter extends GenericFilter {

    private final AuthenticationService service;

    private final AppExceptionHandler handler;

    private final ObjectMapper mapper;

    @Override
    public void doFilter(ServletRequest rawRequest, ServletResponse rawResponse, FilterChain chain) throws IOException, ServletException {
        var request = (HttpServletRequest) rawRequest;
        var response = (HttpServletResponse) rawResponse;
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authHeader != null && !authHeader.isBlank() && authHeader.startsWith("Bearer ")) {
            try {
                String token = authHeader.split(" ")[1];
                Authentication authentication = service.authenticateWithJwtToken(token);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (JwtTokenException exception) {
                var error = handler.handleJwtTokenException(request, exception);
                response.setStatus(error.status());
                rawResponse.setContentType(ContentType.APPLICATION_JSON.getMimeType());
                rawResponse.getWriter().write(mapper.writeValueAsString(error));
                return;
            }
        }
        chain.doFilter(rawRequest, rawResponse);
    }

}
