package degallant.github.io.todoapp.exceptions;

public record ExceptionDetails(String type, String message, String typeCause, String messageCause,
                        StackTraceElement[] stack) {

    ExceptionDetails(Exception exception) {
        this(
                exception.getClass().getSimpleName(),
                exception.getMessage(),
                exception.getCause() != null ? exception.getCause().getClass().getSimpleName() : null,
                exception.getCause() != null ? exception.getCause().getMessage() : null,
                exception.getStackTrace()
        );
    }
}
