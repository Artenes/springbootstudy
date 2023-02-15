package degallant.github.io.todoapp;

import degallant.github.io.todoapp.sanitization.Sanitizer;
import degallant.github.io.todoapp.sanitization.parsers.PrimitiveFieldParser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpHeaders;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.OffsetDateTime;

/**
 * @noinspection ClassCanBeRecord, RedundantThrows, NullableProblems
 */
@Configuration
@RequiredArgsConstructor
public class HeaderValidation implements WebMvcConfigurer, HandlerInterceptor {

    private final Sanitizer sanitizer;
    private final OffsetHolder offsetHolder;
    private final PrimitiveFieldParser parser;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(this);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String language = request.getHeader(HttpHeaders.ACCEPT_LANGUAGE);
        String offset = request.getHeader("Accept-Offset");

        var result = sanitizer.sanitize(
                sanitizer.header(HttpHeaders.ACCEPT_LANGUAGE).withOptionalValue(language).sanitize(parser::toLocale),
                sanitizer.header("Accept-Offset").withOptionalValue(offset).sanitize(parser::toOffset)
        );

        offsetHolder.setOffset(result.get("Accept-Offset").ifNull(OffsetDateTime.now().getOffset()));
        LocaleContextHolder.setLocale(result.get(HttpHeaders.ACCEPT_LANGUAGE).ifNull(LocaleContextHolder.getLocale()));

        return true;
    }
}
