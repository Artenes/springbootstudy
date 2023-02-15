package degallant.github.io.todoapp;

import degallant.github.io.todoapp.sanitization.Sanitizer;
import degallant.github.io.todoapp.sanitization.parsers.PrimitiveFieldParser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.OffsetDateTime;

/**
 * @noinspection ClassCanBeRecord, NullableProblems, RedundantThrows
 */
@Component
@RequiredArgsConstructor
public class ZoneOffsetHeaderInterceptor implements HandlerInterceptor {

    private final Sanitizer sanitizer;
    private final OffsetHolder offsetHolder;
    private final PrimitiveFieldParser parser;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String offset = request.getHeader("Accept-Offset");
        var parsed = sanitizer.sanitizeSingle(
                sanitizer.header("Accept-Offset").withOptionalValue(offset).sanitize(parser::toOffset)
        );
        offsetHolder.setOffset(parsed.ifNull(OffsetDateTime.now().getOffset()));
        return true;
    }

}
