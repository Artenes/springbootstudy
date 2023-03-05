package degallant.github.io.todoapp.i18n;

import degallant.github.io.todoapp.sanitization.InvalidValueException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class Messages {

    private final ResourcePatternResolver resourcePatternResolver;
    private final MessageSource messageSource;
    private List<Locale> supportedLocales;

    public Locale parse(String localesList) throws InvalidValueException {
        try {
            var list = Locale.LanguageRange.parse(localesList);
            var locale = Locale.lookup(list, getSupportedLocales());
            if (locale == null) {
                throw new InvalidValueException("validation.not_a_locale", localesList);
            }
            return locale;
        } catch (NullPointerException | IllegalArgumentException exception) {
            throw new InvalidValueException(exception, "validation.invalid_locale", localesList);
        }
    }

    public void setLocale(Locale locale) {
        LocaleContextHolder.setLocale(locale);
    }

    public String get(String code, Object... args) {
        var locale = LocaleContextHolder.getLocale();
        return messageSource.getMessage(code, args, locale);
    }

    private List<Locale> getSupportedLocales() {
        if (supportedLocales != null) {
            return supportedLocales;
        }

        var locationPattern = "classpath:/language/*";

        try {
            var locales = new ArrayList<Locale>();
            var resources = resourcePatternResolver.getResources(locationPattern);
            for (Resource resource : resources) {

                if (!resource.isFile()) {
                    throw new RuntimeException("Resource is not a file " + resource);
                }

                if (resource.getFilename() == null) {
                    throw new RuntimeException("There is an invalid file name in " + locationPattern);
                }

                var parts = resource.getFilename().split("\\.");

                if (parts.length == 0) {
                    throw new RuntimeException("Invalid filename " + resource.getFilename());
                }

                var regionParts = parts[0].split("_");

                if (regionParts.length == 1) {
                    locales.add(new Locale("en"));
                    continue;
                }

                if (regionParts.length == 2) {
                    locales.add(new Locale(regionParts[1]));
                    continue;
                }

                if (regionParts.length == 3) {
                    locales.add(new Locale(regionParts[1], regionParts[2]));
                }

            }

            supportedLocales = locales;
            return locales;

        } catch (IOException exception) {
            throw new RuntimeException("Failed to list files from pattern " + locationPattern, exception);
        }
    }

}
