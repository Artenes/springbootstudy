package degallant.github.io.todoapp.common;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.hateoas.Link;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class LinkBuilder {

    public PathStep version(int version) {
        return new PathStep(new BuilderArgs(makeBaseUrl(), version));
    }

    private String makeBaseUrl() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (!(requestAttributes instanceof ServletRequestAttributes)) {
            throw new RuntimeException("Was not possible to get base url");
        }
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        return String.format("%s://%s:%d", request.getScheme(), request.getServerName(), request.getServerPort());
    }

    /**
     * @noinspection ClassCanBeRecord
     */
    @RequiredArgsConstructor
    public static class PathStep {

        private final BuilderArgs args;

        public ChildPathStep to(String path) {
            args.paths.add(path);
            return new ChildPathStep(args);
        }

    }

    /**
     * @noinspection ClassCanBeRecord
     */
    @RequiredArgsConstructor
    public static class ChildPathStep {

        private final BuilderArgs args;

        public ChildPathStep slash(Object path) {
            args.getPaths().add(path);
            return this;
        }

        public ParamsStep withParams() {
            return new ParamsStep(args);
        }

        public Link withRel(String rel) {
            String path = args.getPaths().stream().map(Object::toString).reduce("", (first, second) -> first + "/" + second).substring(1);
            String url = args.getBaseUrl() + "/v" + args.getVersion() + "/" + path;
            return Link.of(url).withRel(rel);
        }

        public Link withSelfRel() {
            return withRel("self");
        }

    }

    /**
     * @noinspection ClassCanBeRecord
     */
    @RequiredArgsConstructor
    public static class ParamsStep {

        private final BuilderArgs args;

        public ParamsStep addParam(String name, String value) {
            if (value == null || value.isEmpty()) {
                return this;
            }
            args.getParams().put(name, value);
            return this;
        }

        public ParamsStep addParam(String name, int value) {
            return addParam(name, String.valueOf(value));
        }

        public ParamsStep addPage(int value) {
            return addParam("p", value);
        }

        public ParamsStep addSort(String sort) {
            return addParam("s", sort);
        }

        public Link build() {
            String path = args.getPaths().stream().map(Object::toString).reduce("", (first, second) -> first + "/" + second).substring(1);
            String params = parseParams();
            String url = args.getBaseUrl() + "/v" + args.getVersion() + "/" + path + params;
            return Link.of(url);
        }

        private String parseParams() {
            StringBuilder parsedParams = new StringBuilder();
            for (String key : args.getParams().keySet()) {
                String value = args.getParams().get(key);
                parsedParams.append(key).append("=").append(value);
            }
            if (parsedParams.isEmpty()) {
                return "";
            }
            return "?" + parsedParams;
        }

    }

    @Setter
    @Getter
    private static class BuilderArgs {

        private String baseUrl;
        private int version;
        private List<Object> paths = new ArrayList<>();
        private Map<String, String> params = new HashMap<>();

        public BuilderArgs(String baseUrl, int version) {
            this.baseUrl = baseUrl;
            this.version = version;
        }
    }

}
