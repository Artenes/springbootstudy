package degallant.github.io.todoapp.exceptions;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.google.common.base.CaseFormat;
import degallant.github.io.todoapp.common.SortParsingException;
import degallant.github.io.todoapp.internationalization.Messages;
import degallant.github.io.todoapp.openid.OpenIdExtractionException;
import degallant.github.io.todoapp.validation.InvalidRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.ConversionFailedException;
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

    @ExceptionHandler(InvalidRequestException.class)
    public ErrorResponse handleInvalidRequestException(InvalidRequestException exception) {

        return ErrorResponseBuilder.from(exception)
                .withTitle(messages.get("error.invalidrequest.title"))
                .withDetail(messages.get("error.invalidrequest.detail"))
                .withStatus(HttpStatus.BAD_REQUEST)
                .withType(ErrorType.INVALID_REQUEST)
                .withProperty("errors", exception.getErrors())
                .withDebug(debug)
                .build();

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

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ErrorResponse handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException exception) {

        //TODO standardize how exceptions are handled for validation
        if (exception.getCause() instanceof ConversionFailedException) {
            var invalidValue = ((ConversionFailedException) exception.getCause()).getValue();
            return ErrorResponseBuilder.from(exception)
                    .withTitle(messages.get("error.invalid_query.title"))
                    .withDetail(messages.get("error.invalid_query.detail", invalidValue))
                    .withStatus(HttpStatus.BAD_REQUEST)
                    .withType(ErrorType.INVALID_QUERY_PARAM)
                    .withDebug(debug)
                    .build();
        }

        //this is for IllegalArgumentException, i.e. when invalid values are passed down in the url path
        return ErrorResponseBuilder.from(exception)
                .withTitle(messages.get("error.nosuchelement.title"))
                .withDetail(messages.get("error.nosuchelement.detail"))
                .withStatus(HttpStatus.NOT_FOUND)
                .withType(ErrorType.NO_SUCH_ELEMENT)
                .withDebug(debug)
                .build();

    }

    @ExceptionHandler(NoSuchElementException.class)
    public ErrorResponse handleNoSuchElementException(NoSuchElementException exception) {

        return ErrorResponseBuilder.from(exception)
                .withTitle(messages.get("error.nosuchelement.title"))
                .withDetail(messages.get("error.nosuchelement.detail"))
                .withStatus(HttpStatus.NOT_FOUND)
                .withType(ErrorType.NO_SUCH_ELEMENT)
                .withDebug(debug)
                .build();

    }

    @ExceptionHandler(SortParsingException.class)
    public ErrorResponse handleSortParsingException(SortParsingException exception) {

        var details = "";

        if (exception instanceof SortParsingException.InvalidAttribute) {
            var attribute = ((SortParsingException.InvalidAttribute) exception).getAttribute();
            details = messages.get("error.invalid_sort_attribute.detail", attribute);
        } else if (exception instanceof SortParsingException.InvalidDirection) {
            var direction = ((SortParsingException.InvalidDirection) exception).getDirection();
            details = messages.get("error.invalid_sort_direction.detail", direction);
        } else if (exception instanceof SortParsingException.InvalidQuery) {
            var query = ((SortParsingException.InvalidQuery) exception).getQuery();
            details = messages.get("error.invalid_sort_query.detail", query);
        }

        return ErrorResponseBuilder.from(exception)
                .withTitle(messages.get("error.invalid_sort_title"))
                .withDetail(details)
                .withStatus(HttpStatus.BAD_REQUEST)
                .withType(ErrorType.INVALID_SORT)
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
        return CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, camelCase);
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
