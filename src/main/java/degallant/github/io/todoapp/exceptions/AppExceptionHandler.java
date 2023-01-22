package degallant.github.io.todoapp.exceptions;

import degallant.github.io.todoapp.internationalization.Messages;
import degallant.github.io.todoapp.openid.OpenIdExtractionException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

/** @noinspection unused, ClassCanBeRecord */
@ControllerAdvice
public class AppExceptionHandler {

    private final Messages messages;
    private final boolean debug;

    public AppExceptionHandler(Messages messages, @Value("${app.debug:false}") boolean debug) {
        this.messages = messages;
        this.debug = debug;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ErrorResponse handleValidationException(MethodArgumentNotValidException exception) {

        List<FieldAndError> errors = exception.getFieldErrors().stream().map(
                fieldError -> new FieldAndError(fieldError.getField(), fieldError.getDefaultMessage())
        ).collect(Collectors.toList());

        return ErrorResponseBuilder.from(exception)
                .withTitle(messages.get("error.invalidrequest.title"))
                .withDetail(messages.get("error.invalidrequest.detail"))
                .withStatus(HttpStatus.BAD_REQUEST)
                .withType(ErrorType.INVALID_REQUEST)
                .withProperty("errors", errors)
                .withDebug(debug)
                .build();

    }

    @ExceptionHandler(OpenIdExtractionException.class)
    public ErrorResponse handleOpenIdExtractionException(OpenIdExtractionException exception) {

        return ErrorResponseBuilder.from(exception)
                .withTitle(messages.get("error.invalidtoken.title"))
                .withDetail(messages.get("error.invalidtoken.detail"))
                .withStatus(HttpStatus.BAD_REQUEST)
                .withType(ErrorType.INVALID_TOKEN)
                .withDebug(debug)
                .build();

    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ErrorResponse handleHttpMessageNotReadableException(HttpMessageNotReadableException exception) {

        return ErrorResponseBuilder.from(exception)
                .withTitle(messages.get("error.unprocessable.title"))
                .withDetail(messages.get("error.unprocessable.detail"))
                .withStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                .withType(ErrorType.UNPROCESSABLE_REQUEST)
                .withDebug(debug)
                .build();

    }

    @ExceptionHandler({NoSuchElementException.class, MethodArgumentTypeMismatchException.class})
    public ErrorResponse handleNoSuchElementException(Exception exception) {

        return ErrorResponseBuilder.from(exception)
                .withTitle(messages.get("error.nosuchelement.title"))
                .withDetail(messages.get("error.nosuchelement.detail"))
                .withStatus(HttpStatus.NOT_FOUND)
                .withType(ErrorType.NO_SUCH_ELEMENT)
                .withDebug(debug)
                .build();

    }

    @ExceptionHandler(Exception.class)
    public ErrorResponse handleException(Exception exception) {

        return ErrorResponseBuilder.from(exception)
                .withTitle(messages.get("error.server.title"))
                .withDetail(messages.get("error.server.detail"))
                .withStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                .withType(ErrorType.INTERNAL_SERVER_ERROR)
                .withDebug(debug)
                .build();

    }

    private record FieldAndError(String field, String error) {

    }

}
