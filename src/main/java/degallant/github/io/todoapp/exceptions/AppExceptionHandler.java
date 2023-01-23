package degallant.github.io.todoapp.exceptions;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
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

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @noinspection unused, ClassCanBeRecord, unchecked
 */
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
                fieldError -> new FieldAndError(toSnakeCase(fieldError.getField()), fieldError.getDefaultMessage())
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

        var fieldNames = getFieldNamesFromNotReadableException(exception);
        var defaultErrorMessage = messages.get("validation.invalid.message");

        var errors = fieldNames.stream()
                .map(fieldName -> new FieldAndError(fieldName, defaultErrorMessage))
                .collect(Collectors.toList());

        return ErrorResponseBuilder.from(exception)
                .withTitle(messages.get("error.invalidrequest.title"))
                .withDetail(messages.get("error.invalidrequest.detail"))
                .withStatus(HttpStatus.BAD_REQUEST)
                .withType(ErrorType.INVALID_REQUEST)
                .withProperty("errors", errors)
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

    private String toSnakeCase(String camelCase) {
        return camelCase.replaceAll("(?<!^)([A-Z][a-z])", "_$1").toLowerCase(Locale.ROOT);
    }

    private Set<String> getFieldNamesFromNotReadableException(HttpMessageNotReadableException exception) {
        try {
            Field pathField = null;
            var cause = exception.getCause();
            if (cause instanceof InvalidFormatException) {
                pathField = cause.getClass().getSuperclass().getSuperclass().getDeclaredField("_path");
            } else {
                pathField = cause.getClass().getSuperclass().getDeclaredField("_path");
            }
            pathField.setAccessible(true);
            var field = (LinkedList<JsonMappingException.Reference>) pathField.get(cause);
            return field.stream().map(JsonMappingException.Reference::getFieldName).collect(Collectors.toSet());
        } catch (NullPointerException | NoSuchFieldException | IllegalAccessException | ClassCastException e) {
            return Collections.emptySet();
        }
    }

}
