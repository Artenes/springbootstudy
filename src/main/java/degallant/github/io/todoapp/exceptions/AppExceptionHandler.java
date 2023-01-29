package degallant.github.io.todoapp.exceptions;

import degallant.github.io.todoapp.common.SortParsingException;
import degallant.github.io.todoapp.internationalization.Messages;
import degallant.github.io.todoapp.openid.OpenIdExtractionException;
import degallant.github.io.todoapp.validation.InvalidRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.NoSuchElementException;

/**
 * @noinspection unused, ClassCanBeRecord
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

        printStack(exception);

        return ErrorResponseBuilder.from(exception)
                .withTitle(messages.get("error.invalidrequest.title"))
                .withDetail(messages.get("error.invalidrequest.detail"))
                .withStatus(HttpStatus.BAD_REQUEST)
                .withType(ErrorType.INVALID_REQUEST)
                .withProperty("errors", exception.getErrors())
                .withDebug(debug)
                .build();

    }

    @ExceptionHandler(OpenIdExtractionException.class)
    public ErrorResponse handleOpenIdExtractionException(OpenIdExtractionException exception) {

        printStack(exception);

        return ErrorResponseBuilder.from(exception)
                .withTitle(messages.get("error.invalidtoken.title"))
                .withDetail(messages.get("error.invalidtoken.detail"))
                .withStatus(HttpStatus.BAD_REQUEST)
                .withType(ErrorType.INVALID_TOKEN)
                .withDebug(debug)
                .build();

    }

    @ExceptionHandler(NoSuchElementException.class)
    public ErrorResponse handleNoSuchElementException(NoSuchElementException exception) {

        printStack(exception);

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

        printStack(exception);

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

    public void handle() {
        //TODO add cases for when content type is invalid, we accept only json content as body
        //TODO add case for empty body
    }

    @ExceptionHandler(Exception.class)
    public ErrorResponse handleException(Exception exception) {

        printStack(exception);

        return ErrorResponseBuilder.from(exception)
                .withTitle(messages.get("error.server.title"))
                .withDetail(messages.get("error.server.detail"))
                .withStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                .withType(ErrorType.INTERNAL_SERVER_ERROR)
                .withDebug(debug)
                .build();

    }

    private void printStack(Exception exception) {
        if (debug) {
            exception.printStackTrace();
        }
    }

}
