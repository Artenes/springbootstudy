package degallant.github.io.todoapp.exceptions;

import com.fasterxml.jackson.annotation.JsonInclude;
import degallant.github.io.todoapp.authentication.JwtTokenException;
import degallant.github.io.todoapp.i18n.Messages;
import degallant.github.io.todoapp.openid.OpenIdExtractionException;
import degallant.github.io.todoapp.sanitization.FieldAndErrorMessage;
import degallant.github.io.todoapp.sanitization.FieldAndErrorType;
import degallant.github.io.todoapp.sanitization.InvalidRequestException;
import degallant.github.io.todoapp.sanitization.parsers.SortParsingException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.net.URI;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

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
                .withType(makeType("error.invalidrequest"))
                .withProperty("errors", exception.getErrors().stream().map(this::toFieldAndErrorType).collect(Collectors.toList()))
                .withDebug(debug)
                .build();

    }

    @ExceptionHandler(InvalidStateException.class)
    public ErrorResponse handleInvalidStateException(InvalidStateException exception) {

        printStack(exception);

        return ErrorResponseBuilder.from(exception)
                .withTitle(messages.get("error.invalid_state.title"))
                .withDetail(messages.get(exception.getMessageId(), exception.getArgs()))
                .withStatus(HttpStatus.CONFLICT)
                .withType(makeType(exception.getMessageId()))
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
                .withType(makeType("error.invalidtoken"))
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
                .withType(makeType("error.nosuchelement"))
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
                .withType(makeType("error.invalid_sort"))
                .withDebug(debug)
                .build();

    }

    @ExceptionHandler({HttpMediaTypeNotSupportedException.class, HttpMessageNotReadableException.class})
    public ErrorResponse handleInvalidRequestException(Exception exception) {

        printStack(exception);

        return ErrorResponseBuilder.from(exception)
                .withTitle(messages.get("error.invalidrequesttype.title"))
                .withDetail(messages.get("error.invalidrequesttype.detail"))
                .withStatus(HttpStatus.BAD_REQUEST)
                .withType(makeType("error.invalidrequesttype"))
                .withDebug(debug)
                .build();

    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ErrorResponse handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException exception) {

        printStack(exception);

        return ErrorResponseBuilder.from(exception)
                .withTitle(messages.get("error.notsupported.title"))
                .withDetail(messages.get("error.notsupported.detail", exception.getMethod()))
                .withStatus(HttpStatus.METHOD_NOT_ALLOWED)
                .withType(makeType("error.notsupported"))
                .withDebug(debug)
                .build();

    }

    @ExceptionHandler(Exception.class)
    public ErrorResponse handleException(Exception exception) {

        printStack(exception);

        return ErrorResponseBuilder.from(exception)
                .withTitle(messages.get("error.server.title"))
                .withDetail(messages.get("error.server.detail"))
                .withStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                .withType(makeType("error.server"))
                .withDebug(debug)
                .build();

    }

    @ExceptionHandler(JwtTokenException.class)
    public ErrorResponse handleJwtTokenException(JwtTokenException exception) {

        printStack(exception);

        var errorId = "error.invalid_token";

        if (exception instanceof JwtTokenException.Expired) {
            errorId = "error.token_expired";
        }

        if (exception instanceof JwtTokenException.InvalidClaim) {
            errorId = "error.token_tempered";
        }

        if (exception instanceof JwtTokenException.InvalidSubject) {
            errorId = "error.token_unknown_subject";
        }

        String detail = messages.get(errorId, exception.getToken());
        URI type = makeType(errorId);

        return ErrorResponseBuilder.from(exception)
                .withTitle(messages.get("error.invalid_token.title"))
                .withDetail(detail)
                .withStatus(HttpStatus.BAD_REQUEST)
                .withType(type)
                .withDebug(debug)
                .build();

    }

    public Error handleJwtTokenException(HttpServletRequest request, JwtTokenException exception) {

        var problemDetail = handleJwtTokenException(exception).getBody();

        var exceptionDetail = debug ? new ExceptionDetails(exception) : null;
        return new Error(
                problemDetail.getType(),
                problemDetail.getTitle(),
                problemDetail.getStatus(),
                problemDetail.getDetail(),
                request.getServletPath(),
                exceptionDetail
        );

    }

    public Error handleException(HttpServletRequest request, InvalidStateException exception) {

        var problemDetail = handleInvalidStateException(exception).getBody();

        var exceptionDetail = debug ? new ExceptionDetails(exception) : null;
        return new Error(
                problemDetail.getType(),
                problemDetail.getTitle(),
                problemDetail.getStatus(),
                problemDetail.getDetail(),
                request.getServletPath(),
                exceptionDetail
        );

    }

    private void printStack(Exception exception) {
        if (debug) {
            exception.printStackTrace();
        }
    }

    private FieldAndErrorType toFieldAndErrorType(FieldAndErrorMessage error) {
        return new FieldAndErrorType(
                error.field(),
                "https://todoapp.com/" + error.errorId(),
                messages.get(error.errorId(), error.errorArgs()),
                error.origin()
        );
    }

    private URI makeType(String type) {
        return URI.create("https://todoapp.com/" + type);
    }

    public record Error(
            URI type,
            String title,
            int status,
            String detail,
            String instance,
            @JsonInclude(JsonInclude.Include.NON_NULL)
            ExceptionDetails exception) {
    }

}
