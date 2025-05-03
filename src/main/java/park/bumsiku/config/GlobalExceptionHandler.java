package park.bumsiku.config;

import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import park.bumsiku.domain.dto.response.Response;

import java.util.NoSuchElementException;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Response<Void>> handleConstraintViolationException(IllegalArgumentException e) {
        Response<Void> response = Response.error(
                400,
                e.getMessage() != null ? e.getMessage() : "Invalid Argument"
        );
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Response<Void>> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        Response<Void> response = Response.error(
                400,
                "Invalid request body"
        );
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Response<Void>> handleConstraintViolationException(ConstraintViolationException e) {
        log.error("Validation error: {}", e.getMessage());
        Response<Void> response = Response.error(
                400,
                e.getMessage() != null ? e.getMessage() : "유효성 검증 오류"
        );
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Response<Void>> handleNoSuchArgumentException(NoSuchElementException e) {
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

        Response<Void> response = Response.error(400, detail);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Response<Void>> handleUnhandledException(Exception e) {
        log.error("Unhandled exception occurred:", e);
        Response<Void> response = Response.error(
                500,
                "Internal Server Error"
        );
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}