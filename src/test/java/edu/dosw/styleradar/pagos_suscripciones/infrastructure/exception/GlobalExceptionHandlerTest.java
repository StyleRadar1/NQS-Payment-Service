package edu.dosw.styleradar.pagos_suscripciones.infrastructure.exception;

import edu.dosw.styleradar.pagos_suscripciones.domain.exception.*;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @Mock private HttpServletRequest request;
    @InjectMocks private GlobalExceptionHandler handler;

    private void stubPath() {
        when(request.getRequestURI()).thenReturn("/api/v1/test");
    }

    @Test
    void planNotFoundMapsTo404() {
        stubPath();
        ResponseEntity<ErrorResponse> res = handler.handlePlanNotFound(
            new PlanNotFoundException("Plan 99 no existe"), request);
        assertEquals(HttpStatus.NOT_FOUND, res.getStatusCode());
        assertEquals("PLAN_NOT_FOUND", res.getBody().error());
        assertEquals("/api/v1/test", res.getBody().path());
    }

    @Test
    void suscripcionNotFoundMapsTo404() {
        stubPath();
        ResponseEntity<ErrorResponse> res = handler.handleSuscripcionNotFound(
            new SuscripcionNoEncontradaException("No existe"), request);
        assertEquals(HttpStatus.NOT_FOUND, res.getStatusCode());
        assertEquals("SUSCRIPCION_NOT_FOUND", res.getBody().error());
    }

    @Test
    void paymentProcessingMapsTo422() {
        stubPath();
        ResponseEntity<ErrorResponse> res = handler.handlePaymentProcessing(
            new PaymentProcessingException("Orden no encontrada"), request);
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, res.getStatusCode());
        assertEquals("PAYMENT_PROCESSING_ERROR", res.getBody().error());
    }

    @Test
    void idempotencyMapsTo409() {
        stubPath();
        ResponseEntity<ErrorResponse> res = handler.handleIdempotency(
            new IdempotencyException("Lock no adquirido"), request);
        assertEquals(HttpStatus.CONFLICT, res.getStatusCode());
        assertEquals("IDEMPOTENCY_CONFLICT", res.getBody().error());
    }

    @Test
    void pagoYaProcesadoMapsTo409() {
        stubPath();
        ResponseEntity<ErrorResponse> res = handler.handlePagoYaProcesado(
            new PagoYaProcesadoException("MP-1"), request);
        assertEquals(HttpStatus.CONFLICT, res.getStatusCode());
        assertEquals("PAYMENT_ALREADY_PROCESSED", res.getBody().error());
    }

    @Test
    void securityMapsTo403() {
        stubPath();
        ResponseEntity<ErrorResponse> res = handler.handleSecurity(
            new SecurityException("No autorizado"), request);
        assertEquals(HttpStatus.FORBIDDEN, res.getStatusCode());
        assertEquals("FORBIDDEN", res.getBody().error());
    }

    @Test
    void illegalStateMapsTo400() {
        stubPath();
        ResponseEntity<ErrorResponse> res = handler.handleIllegalState(
            new IllegalStateException("Solo se reembolsan pagos aprobados"), request);
        assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());
        assertEquals("INVALID_STATE", res.getBody().error());
    }

    @Test
    void illegalArgumentMapsTo400() {
        stubPath();
        ResponseEntity<ErrorResponse> res = handler.handleIllegalArgument(
            new IllegalArgumentException("Tipo de suscriptor distinto"), request);
        assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());
        assertEquals("INVALID_ARGUMENT", res.getBody().error());
    }

    @Test
    void genericExceptionMapsTo500SinExponerDetalles() {
        stubPath();
        ResponseEntity<ErrorResponse> res = handler.handleGeneric(
            new RuntimeException("NullPointerException en línea 42"), request);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, res.getStatusCode());
        assertEquals("INTERNAL_ERROR", res.getBody().error());
        // No expone el stacktrace interno (SR-RNF-02)
        assertFalse(res.getBody().message().contains("NullPointerException"));
    }
}