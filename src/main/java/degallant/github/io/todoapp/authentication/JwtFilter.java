package degallant.github.io.todoapp.authentication;

import com.fasterxml.jackson.databind.ObjectMapper;
import degallant.github.io.todoapp.exceptions.AppExceptionHandler;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.http.entity.ContentType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;

@RequiredArgsConstructor
public class JwtFilter extends GenericFilter {

    private final AuthenticationService service;
    private final AppExceptionHandler handler;
    private final ObjectMapper mapper;

    @Override
    public void doFilter(ServletRequest rawRequest, ServletResponse rawResponse, FilterChain chain) throws IOException, ServletException {
        var request = new AuthorizedRequest((HttpServletRequest) rawRequest);
        var response = (HttpServletResponse) rawResponse;
        var token = request.getToken();
        if (token != null) {
            try {
                Authentication authentication = service.authenticateWithJwtToken(request.getToken());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (JwtTokenException exception) {
                var error = handler.handleJwtTokenException(request.raw(), exception);
                response.setStatus(error.status());
                rawResponse.setContentType(ContentType.APPLICATION_JSON.getMimeType());
                rawResponse.getWriter().write(mapper.writeValueAsString(error));
                return;
            }
        }
        chain.doFilter(rawRequest, rawResponse);
    }

}
