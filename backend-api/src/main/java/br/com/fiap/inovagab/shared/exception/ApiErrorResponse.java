package br.com.fiap.inovagab.shared.exception;

import java.time.Instant;
import java.util.List;

import org.springframework.http.HttpStatus;

public record ApiErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String code,
        String message,
        String path,
        String requestId,
        List<FieldViolation> fieldErrors
) {

    public static ApiErrorResponse of(
            HttpStatus status,
            String code,
            String message,
            String path,
            String requestId
    ) {
        return new ApiErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                code,
                message,
                path,
                requestId,
                List.of()
        );
    }

    public static ApiErrorResponse validation(
            String message,
            String path,
            String requestId,
            List<FieldViolation> violations
    ) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        return new ApiErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                "VALIDATION_ERROR",
                message,
                path,
                requestId,
                violations
        );
    }

    public record FieldViolation(String field, String message) {
    }
}
