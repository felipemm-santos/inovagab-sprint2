package br.com.fiap.inovagab.shared.exception;

import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import br.com.fiap.inovagab.shared.web.RequestIdFilter;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiErrorResponse> handleApiException(
            ApiException exception,
            HttpServletRequest request
    ) {
        var body = ApiErrorResponse.of(
                exception.getStatus(),
                exception.getCode(),
                exception.getMessage(),
                request.getRequestURI(),
                requestId(request)
        );
        return ResponseEntity.status(exception.getStatus()).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        List<ApiErrorResponse.FieldViolation> violations = exception
                .getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> new ApiErrorResponse.FieldViolation(
                        error.getField(),
                        error.getDefaultMessage()
                ))
                .toList();

        var body = ApiErrorResponse.validation(
                "One or more fields are invalid",
                request.getRequestURI(),
                requestId(request),
                violations
        );
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleUnreadableMessage(
            HttpMessageNotReadableException exception,
            HttpServletRequest request
    ) {
        var body = ApiErrorResponse.of(
                HttpStatus.BAD_REQUEST,
                "INVALID_REQUEST",
                "Request body is malformed or contains an invalid value",
                request.getRequestURI(),
                requestId(request)
        );
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler({
            ConstraintViolationException.class,
            MethodArgumentTypeMismatchException.class
    })
    public ResponseEntity<ApiErrorResponse> handleInvalidParameter(
            Exception exception,
            HttpServletRequest request
    ) {
        var body = ApiErrorResponse.of(
                HttpStatus.BAD_REQUEST,
                "INVALID_PARAMETER",
                "One or more request parameters are invalid",
                request.getRequestURI(),
                requestId(request)
        );
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicateKey(
            DuplicateKeyException exception,
            HttpServletRequest request
    ) {
        var body = ApiErrorResponse.of(
                HttpStatus.CONFLICT,
                "DUPLICATE_RESOURCE",
                "A resource with the same unique data already exists",
                request.getRequestURI(),
                requestId(request)
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodNotAllowed(
            HttpRequestMethodNotSupportedException exception,
            HttpServletRequest request
    ) {
        var body = ApiErrorResponse.of(
                HttpStatus.METHOD_NOT_ALLOWED,
                "METHOD_NOT_ALLOWED",
                "The HTTP method is not supported for this resource",
                request.getRequestURI(),
                requestId(request)
        );
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpectedException(
            Exception exception,
            HttpServletRequest request
    ) {
        String requestId = requestId(request);
        log.error("Unexpected API error. requestId={}", requestId, exception);

        var body = ApiErrorResponse.of(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "INTERNAL_ERROR",
                "An unexpected error occurred",
                request.getRequestURI(),
                requestId
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    private String requestId(HttpServletRequest request) {
        Object value = request.getAttribute(RequestIdFilter.REQUEST_ID_ATTRIBUTE);
        return value == null ? null : value.toString();
    }
}
