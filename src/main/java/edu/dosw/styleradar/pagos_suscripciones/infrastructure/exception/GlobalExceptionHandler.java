package edu.dosw.styleradar.pagos_suscripciones.infrastructure.exception;

import edu.dosw.styleradar.pagos_suscripciones.domain.exception.IdempotencyException;
import edu.dosw.styleradar.pagos_suscripciones.domain.exception.PagoYaProcesadoException;
import edu.dosw.styleradar.pagos_suscripciones.domain.exception.PaymentProcessingException;
import edu.dosw.styleradar.pagos_suscripciones.domain.exception.PlanNotFoundException;
import edu.dosw.styleradar.pagos_suscripciones.domain.exception.SuscripcionNoEncontradaException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ---------- 404 ----------
    @ExceptionHandler(PlanNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePlanNotFound(PlanNotFoundException ex, HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, "PLAN_NOT_FOUND", ex.getMessage(), req);
    }

    @ExceptionHandler(SuscripcionNoEncontradaException.class)
    public ResponseEntity<ErrorResponse> handleSuscripcionNotFound(SuscripcionNoEncontradaException ex, HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, "SUSCRIPCION_NOT_FOUND", ex.getMessage(), req);
    }

    // ---------- 422 ----------
    @ExceptionHandler(PaymentProcessingException.class)
    public ResponseEntity<ErrorResponse> handlePaymentProcessing(PaymentProcessingException ex, HttpServletRequest req) {
        return build(HttpStatus.UNPROCESSABLE_ENTITY, "PAYMENT_PROCESSING_ERROR", ex.getMessage(), req);
    }

    // ---------- 409 ----------
    @ExceptionHandler(IdempotencyException.class)
    public ResponseEntity<ErrorResponse> handleIdempotency(IdempotencyException ex, HttpServletRequest req) {
        return build(HttpStatus.CONFLICT, "IDEMPOTENCY_CONFLICT", ex.getMessage(), req);
    }

    @ExceptionHandler(PagoYaProcesadoException.class)
    public ResponseEntity<ErrorResponse> handlePagoYaProcesado(PagoYaProcesadoException ex, HttpServletRequest req) {
        return build(HttpStatus.CONFLICT, "PAYMENT_ALREADY_PROCESSED", ex.getMessage(), req);
    }

    // ---------- 403 ----------
    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<ErrorResponse> handleSecurity(SecurityException ex, HttpServletRequest req) {
        return build(HttpStatus.FORBIDDEN, "FORBIDDEN", ex.getMessage(), req);
    }

    // ---------- 400 ----------
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(IllegalStateException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "INVALID_STATE", ex.getMessage(), req);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "INVALID_ARGUMENT", ex.getMessage(), req);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("Datos inválidos");
        return build(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", message, req);
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ErrorResponse> handleMissingHeader(MissingRequestHeaderException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "MISSING_HEADER",
                "Falta el header requerido: " + ex.getHeaderName(), req);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParameter(MissingServletRequestParameterException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "MISSING_PARAMETER",
                "Falta el parámetro requerido: " + ex.getParameterName(), req);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest req) {
        String message = "Valor inválido para el parámetro '" + ex.getName() + "': " + ex.getValue();
        return build(HttpStatus.BAD_REQUEST, "INVALID_PARAMETER", message, req);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadable(HttpMessageNotReadableException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "MALFORMED_BODY",
                "El cuerpo de la solicitud no pudo interpretarse como JSON válido.", req);
    }

    // ---------- 500 ----------
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, HttpServletRequest req) {
        log.error("Error inesperado en {}: {}", req.getRequestURI(), ex.getMessage(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR",
                "Error inesperado en el servidor. Contacte al administrador.", req);
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String code, String message, HttpServletRequest req) {
        ErrorResponse body = new ErrorResponse(
                LocalDateTime.now(),
                status.value(),
                code,
                message,
                req.getRequestURI()
        );
        return ResponseEntity.status(status).body(body);
    }
}