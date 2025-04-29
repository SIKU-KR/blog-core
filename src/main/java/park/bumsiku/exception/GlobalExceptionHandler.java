package park.bumsiku.exception;

import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import park.bumsiku.domain.dto.Response;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Response<Void>> handleConstraintViolationException(IllegalArgumentException e) {
        Response<Void> response = Response.<Void>error(
                400,
                e.getMessage() != null ? e.getMessage() : "Invalid Argument"
        );
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Response<Void>> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        Response<Void> response = Response.<Void>error(
                400,
                "Invalid request body"
        );
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(PostNotFoundException.class)
    public ResponseEntity<Response<Void>> handlePostNotFoundException(PostNotFoundException e) {
        Response<Void> response = Response.<Void>error(
                404,
                e.getMessage() != null ? e.getMessage() : "Post not found"
        );
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Response<Void>> handleConstraintViolationException(ConstraintViolationException e) {
        log.error("Validation error: {}", e.getMessage());
        Response<Void> response = Response.<Void>error(
                400,
                e.getMessage() != null ? e.getMessage() : "유효성 검증 오류"
        );
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Response<Void>> handleUnhandledException(Exception e) {
        log.error("Unhandled exception occurred:", e);
        Response<Void> response = Response.<Void>error(
                500,
                "Internal Server Error"
        );
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}