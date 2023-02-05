package degallant.github.io.todoapp.internationalization;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

/** @noinspection ClassCanBeRecord*/
@Component
@RequiredArgsConstructor
public class Messages {

    private final MessageSource messageSource;

    public String get(String code, Object... args) {
        return messageSource.getMessage(code, args, LocaleContextHolder.getLocale());
    }

}
