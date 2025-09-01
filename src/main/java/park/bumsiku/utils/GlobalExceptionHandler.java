package park.bumsiku.utils;

import jakarta.validation.ConstraintViolationException;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import park.bumsiku.domain.dto.response.Response;

import java.util.NoSuchElementException;

import static park.bumsiku.log.LoggingConstants.UNKNOWN;

@ControllerAdvice
@AllArgsConstructor
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final DiscordWebhookCreator discord;

    private ResponseEntity<Response<Void>> handleException(String logMessage, Exception e, HttpStatus status, String errorMessage, String defaultMessage) {
        log.warn(logMessage, e.getMessage());
        String message = errorMessage != null ? errorMessage : (e.getMessage() != null ? e.getMessage() : defaultMessage);
        Response<Void> response = Response.error(status.value(), message);
        return new ResponseEntity<>(response, status);
    }

    private ResponseEntity<Response<Void>> handleErrorException(Exception e, String logDetails) {
        log.error("Unhandled exception occurred: {}\n{}", e.getMessage(), logDetails);
        Response<Void> response = Response.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal Server Error");
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Response<Void>> handleConstraintViolationException(IllegalArgumentException e) {
        return handleException("Invalid argument: {}", e, HttpStatus.BAD_REQUEST, null, "Invalid Argument");
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Response<Void>> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        return handleException("Invalid request body: {}", e, HttpStatus.BAD_REQUEST, "Invalid request body", null);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Response<Void>> handleConstraintViolationException(ConstraintViolationException e) {
        return handleException("Validation error: {}", e, HttpStatus.BAD_REQUEST, null, "유효성 검증 오류");
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Response<Void>> handleNoSuchArgumentException(NoSuchElementException e) {
        return handleException("Resource not found: {}", e, HttpStatus.NOT_FOUND, null, "Argument not found");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Response<Void>> handleTypeMismatchException(MethodArgumentTypeMismatchException e) {
        String param = e.getName();
        String expectedType = e.getRequiredType() != null ? e.getRequiredType().getSimpleName() : UNKNOWN;
        Object value = e.getValue();
        String detail = String.format("Parameter '%s' must be of type '%s' but value '%s' is invalid", param, expectedType, value);

        return handleException("Type mismatch error: {}", e, HttpStatus.BAD_REQUEST, detail, null);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Response<Void>> handleNoResourceFoundException(NoResourceFoundException e) {
        return handleException("Resource not found: {}", e, HttpStatus.NOT_FOUND, e.getMessage(), null);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Response<Void>> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        return handleException("Http method not supported: {}", e, HttpStatus.METHOD_NOT_ALLOWED, e.getMessage(), null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Response<Void>> handleUnhandledException(Exception e) {
        StackTraceElement[] stackTrace = e.getStackTrace();
        int maxLines = Math.min(2, stackTrace.length);
        StringBuilder partialTrace = new StringBuilder();
        partialTrace.append(e).append("\n");
        for (int i = 0; i < maxLines; i++) {
            partialTrace.append("\tat ").append(stackTrace[i].toString()).append("\n");
        }
        String requestTrace = "Request: " + Thread.currentThread().getName();

        discord.sendMessage("Unhandled exception occurred: " + e.getMessage() + "\n" + partialTrace + "\n" + requestTrace);

        return handleErrorException(e, partialTrace.toString());
    }
}
