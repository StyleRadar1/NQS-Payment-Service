package edu.dosw.styleradar.pagos.infrastructure.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Estructura única de error de StyleRadar (ADR-10).
 *
 * <p>Coincide con el contrato del README: timestamp, status, error, message
 * y path; agrega {@code validationErrors} cuando falla la validación de
 * campos, igual que ms-usuarios.</p>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        Map<String, String> validationErrors) {

    public static ErrorResponse of(int status, String error, String message, String path) {
        return new ErrorResponse(LocalDateTime.now(), status, error, message, path, null);
    }

    public static ErrorResponse of(int status,
                                   String error,
                                   String message,
                                   String path,
                                   Map<String, String> validationErrors) {
        return new ErrorResponse(LocalDateTime.now(), status, error, message, path, validationErrors);
    }
}
