package edu.dosw.styleradar.pagos.infrastructure.exception;

import edu.dosw.styleradar.pagos.domain.exception.ConflictoException;
import edu.dosw.styleradar.pagos.domain.exception.NoAutorizadoException;
import edu.dosw.styleradar.pagos.domain.exception.OperacionNoPermitidaException;
import edu.dosw.styleradar.pagos.domain.exception.PagosException;
import edu.dosw.styleradar.pagos.domain.exception.PaymentProcessingException;
import edu.dosw.styleradar.pagos.domain.exception.RecursoNoEncontradoException;
import edu.dosw.styleradar.pagos.domain.exception.SolicitudInvalidaException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Manejo global de errores (ADR-10).
 *
 * <p>400 para solicitudes inválidas, 401 sin identidad, 403 sin permisos,
 * 404 recursos inexistentes, 409 conflictos de estado o idempotencia,
 * 422 fallos de la pasarela y 500 sin stacktrace.</p>
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> validacion(MethodArgumentNotValidException ex,
                                                    HttpServletRequest request) {
        Map<String, String> errores = new LinkedHashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errores.putIfAbsent(fieldError.getField(),
                    fieldError.getDefaultMessage() == null
                            ? "Valor inválido"
                            : fieldError.getDefaultMessage());
        }
        return build(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR",
                "La solicitud contiene datos inválidos", request, errores);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> cuerpoIlegible(HttpMessageNotReadableException ex,
                                                        HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR",
                "La solicitud contiene datos inválidos", request, null);
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ErrorResponse> headerFaltante(MissingRequestHeaderException ex,
                                                        HttpServletRequest request) {
        return build(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED",
                "Debe iniciar sesión para continuar.", request, null);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> tipoInvalido(MethodArgumentTypeMismatchException ex,
                                                      HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, "BAD_REQUEST",
                "La solicitud contiene parámetros inválidos", request, null);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> integridad(DataIntegrityViolationException ex,
                                                    HttpServletRequest request) {
        log.warn("Conflicto de integridad: {}", ex.getMostSpecificCause().getMessage());
        return build(HttpStatus.CONFLICT, "CONFLICT",
                "Ya existe una suscripción activa o pendiente para este suscriptor.", request, null);
    }

    @ExceptionHandler(PagosException.class)
    public ResponseEntity<ErrorResponse> negocio(PagosException ex, HttpServletRequest request) {
        HttpStatus status = resolverStatus(ex);
        return build(status, ex.getCodigo(), ex.getMessage(), request, null);
    }

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<ErrorResponse> regla(RuntimeException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, "INVALID_STATE", ex.getMessage(), request, null);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> denegado(AccessDeniedException ex, HttpServletRequest request) {
        return build(HttpStatus.FORBIDDEN, "FORBIDDEN",
                "No tiene permisos para realizar esta operación.", request, null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> inesperado(Exception ex, HttpServletRequest request) {
        log.error("Error inesperado en {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR",
                "Ocurrió un error inesperado. Inténtalo nuevamente.", request, null);
    }

    private HttpStatus resolverStatus(PagosException ex) {
        if (ex instanceof NoAutorizadoException) {
            return HttpStatus.UNAUTHORIZED;
        }
        if (ex instanceof OperacionNoPermitidaException) {
            return HttpStatus.FORBIDDEN;
        }
        if (ex instanceof RecursoNoEncontradoException) {
            return HttpStatus.NOT_FOUND;
        }
        if (ex instanceof ConflictoException) {
            return HttpStatus.CONFLICT;
        }
        if (ex instanceof PaymentProcessingException) {
            return HttpStatus.UNPROCESSABLE_ENTITY;
        }
        if (ex instanceof SolicitudInvalidaException) {
            return HttpStatus.BAD_REQUEST;
        }
        return HttpStatus.BAD_REQUEST;
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status,
                                                String error,
                                                String message,
                                                HttpServletRequest request,
                                                Map<String, String> validationErrors) {
        ErrorResponse body = ErrorResponse.of(status.value(), error, message, request.getRequestURI(),
                validationErrors);
        return ResponseEntity.status(status).body(body);
    }
}
