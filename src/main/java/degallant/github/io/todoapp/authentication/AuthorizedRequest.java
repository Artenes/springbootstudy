package degallant.github.io.todoapp.authentication;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;

public record AuthorizedRequest(HttpServletRequest raw) {

    public String getTimeZone() {
        return raw.getHeader("Accept-Offset");
    }

    public String getAuthorization() {
        return raw.getHeader(HttpHeaders.AUTHORIZATION);
    }

    public String getLanguage() {
        return raw.getHeader(HttpHeaders.ACCEPT_LANGUAGE);
    }

    public String getToken() {
        return getTokenFromHeader(getAuthorization());
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

    @Override
    public String toString() {
        return "AuthorizedRequest{auth=" + getAuthorization() + ", lang=" + getLanguage() + ", offset=" + getTimeZone() + "}";
    }
}
