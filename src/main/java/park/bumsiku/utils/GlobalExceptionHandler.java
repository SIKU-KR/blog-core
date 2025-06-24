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

@ControllerAdvice
@AllArgsConstructor
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final DiscordWebhookCreator discord;

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Response<Void>> handleConstraintViolationException(IllegalArgumentException e) {
        log.warn("Invalid argument: {}", e.getMessage());
        Response<Void> response = Response.error(
                400,
                e.getMessage() != null ? e.getMessage() : "Invalid Argument"
        );
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Response<Void>> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        log.warn("Invalid request body: {}", e.getMessage());
        Response<Void> response = Response.error(
                400,
                "Invalid request body"
        );
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Response<Void>> handleConstraintViolationException(ConstraintViolationException e) {
        log.warn("Validation error: {}", e.getMessage());
        Response<Void> response = Response.error(
                400,
                e.getMessage() != null ? e.getMessage() : "유효성 검증 오류"
        );
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Response<Void>> handleNoSuchArgumentException(NoSuchElementException e) {
        log.warn("Resource not found: {}", e.getMessage());
        Response<Void> response = Response.error(
                404,
                e.getMessage() != null ? e.getMessage() : "Argument not found"
        );
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Response<Void>> handleTypeMismatchException(MethodArgumentTypeMismatchException e) {
        // 잘못 전달된 파라미터 이름, 타입, 값 정보를 조합해서 메시지를 구성
        String param = e.getName();
        String expectedType = e.getRequiredType() != null
                ? e.getRequiredType().getSimpleName()
                : "unknown";
        Object value = e.getValue();
        String detail = String.format("Parameter '%s' must be of type '%s' but value '%s' is invalid",
                param, expectedType, value);

        log.warn("Type mismatch error: {}", detail);
        Response<Void> response = Response.error(400, detail);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Response<Void>> handleNoResourceFoundException(NoResourceFoundException e) {
        log.warn("Resource not found: {}", e.getMessage());
        Response<Void> response = Response.error(HttpStatus.NOT_FOUND.value(), e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Response<Void>> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        log.warn("Http method not supported: {}", e.getMessage());
        Response<Void> response = Response.error(HttpStatus.METHOD_NOT_ALLOWED.value(), e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Response<Void>> handleUnhandledException(Exception e) {
        StackTraceElement[] stackTrace = e.getStackTrace();
        int maxLines = Math.min(2, stackTrace.length); // Reduced to 2 lines max
        StringBuilder partialTrace = new StringBuilder();
        partialTrace.append(e).append("\n");
        for (int i = 0; i < maxLines; i++) {
            partialTrace.append("\tat ").append(stackTrace[i].toString()).append("\n");
        }
        String requestTrace = "Request: " + Thread.currentThread().getName();
        log.error("Unhandled exception occurred: {}\n{}", e.getMessage(), partialTrace);
        discord.sendMessage("Unhandled exception occurred: " + e.getMessage() + "\n" + partialTrace + "\n" + requestTrace);
        Response<Void> response = Response.error(
                500,
                "Internal Server Error"
        );
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
