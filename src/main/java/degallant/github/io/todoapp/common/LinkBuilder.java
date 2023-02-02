package degallant.github.io.todoapp.common;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.hateoas.Link;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.net.URISyntaxException;
import java.util.*;

public class LinkBuilder {

    private final String root;
    private final List<Object> paths = new ArrayList<>();
    private final Map<String, String> params = new HashMap<>();

    //TODO remove static methods
    //TODO hide versioning behind builder
    public static LinkBuilder makeLink(Object... paths) {
        try {
            var builder = new LinkBuilder(makeBaseUrl());
            return builder.append(paths);
        } catch (URISyntaxException exception) {
            throw new RuntimeException(exception);
        }
    }

    public static Link makeLinkTo(Object... paths) {
        var linkBuilder = makeLink();
        linkBuilder.append(paths);
        return linkBuilder.build();
    }

    private LinkBuilder(String root) {
        this.root = root;
    }

    public LinkBuilder append(Object... paths) {
        this.paths.addAll(Arrays.stream(paths).toList());
        return this;
    }

    public LinkBuilder addParam(String name, String value) {
        if (value == null || value.isEmpty()) {
            return this;
        }
        params.put(name, value);
        return this;
    }

    public LinkBuilder addParam(String name, int value) {
        return addParam(name, String.valueOf(value));
    }

    public LinkBuilder addPage(int value) {
        return addParam("p", value);
    }

    public LinkBuilder addSort(String sort) {
        return addParam("s", sort);
    }

    public Link build() {
        String path = paths.stream().map(Object::toString).reduce("", (first, second) -> first + "/" + second).substring(1);
        String params = parseParams();
        String url = root + "/" + path + params;
        return Link.of(url);
    }

    private String parseParams() {
        StringBuilder parsedParams = new StringBuilder();
        for (String key : params.keySet()) {
            String value = params.get(key);
            parsedParams.append(key).append("=").append(value);
        }
        if (parsedParams.isEmpty()) {
            return "";
        }
        return "?" + parsedParams;
    }

    private static String makeBaseUrl() throws URISyntaxException {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (!(requestAttributes instanceof ServletRequestAttributes)) {
            throw new RuntimeException("Was not possible to get base url");
        }
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        return String.format("%s://%s:%d", request.getScheme(), request.getServerName(), request.getServerPort());
    }

}
