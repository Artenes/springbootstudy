package degallant.github.io.todoapp;

import degallant.github.io.todoapp.authentication.ApiKeyEntity;
import degallant.github.io.todoapp.authentication.ApiKeyRepository;
import degallant.github.io.todoapp.i18n.Messages;
import degallant.github.io.todoapp.sanitization.Sanitizer;
import degallant.github.io.todoapp.sanitization.parsers.ApiKeyFieldParser;
import degallant.github.io.todoapp.sanitization.parsers.PrimitiveFieldParser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.OffsetDateTime;
import java.util.Locale;

/**
 * @noinspection ClassCanBeRecord, RedundantThrows, NullableProblems
 */
@Configuration
@RequiredArgsConstructor
public class HeaderValidation implements WebMvcConfigurer, HandlerInterceptor {

    private final Sanitizer sanitizer;
    private final OffsetHolder offsetHolder;
    private final PrimitiveFieldParser parser;
    private final ApiKeyFieldParser apiKeyParser;
    private final ApiKeyRepository apiKeyRepository;
    private final Messages messages;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(this);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String language = request.getHeader(HttpHeaders.ACCEPT_LANGUAGE);
        String offset = request.getHeader("Accept-Offset");
        String apiKey = request.getHeader("Client-Agent");

        var result = sanitizer.sanitize(
                sanitizer.header(HttpHeaders.ACCEPT_LANGUAGE).withOptionalValue(language).sanitize(messages::parse),
                sanitizer.header("Accept-Offset").withOptionalValue(offset).sanitize(parser::toOffset),
                sanitizer.header("Client-Agent").withRequiredValue(apiKey).sanitize(apiKeyParser::toApiKeyOrThrowInvalidValue)
        );

        offsetHolder.setOffset(null);
        messages.setLocale(null);

        result.get("Accept-Offset").consumeIfExists(offsetHolder::setOffset);
        result.get(HttpHeaders.ACCEPT_LANGUAGE).consumeIfExistsAs(Locale.class, messages::setLocale);

        var apiKeyEntity = result.get("Client-Agent").as(ApiKeyEntity.class);
        apiKeyEntity.setLastAccess(OffsetDateTime.now());
        apiKeyRepository.save(apiKeyEntity);

        return true;
    }
}
