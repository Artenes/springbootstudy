package degallant.github.io.todoapp.authentication;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;

public record AuthorizedRequest(HttpServletRequest raw) {

    public String getHeader() {
        return raw.getHeader(HttpHeaders.AUTHORIZATION);
    }

    public String getToken() {
        return getTokenFromHeader(getHeader());
    }

    private String getTokenFromHeader(String header) {
        if (header == null || !header.startsWith("Bearer")) {
            return null;
        }
        String[] parts = header.split(" ");
        if (parts.length != 2) {
            return null;
        }
        return parts[1];
    }

}
