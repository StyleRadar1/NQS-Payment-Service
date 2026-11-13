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
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

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
    }

    @Test
    void suscripcionNotFoundMapsTo404() {
        stubPath();
        ResponseEntity<ErrorResponse> res = handler.handleSuscripcionNotFound(
            new SuscripcionNoEncontradaException("No existe"), request);
        assertEquals(HttpStatus.NOT_FOUND, res.getStatusCode());
    }

    @Test
    void paymentProcessingMapsTo422() {
        stubPath();
        ResponseEntity<ErrorResponse> res = handler.handlePaymentProcessing(
            new PaymentProcessingException("Orden no encontrada"), request);
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, res.getStatusCode());
    }

    @Test
    void idempotencyMapsTo409() {
        stubPath();
        ResponseEntity<ErrorResponse> res = handler.handleIdempotency(
            new IdempotencyException("Lock no adquirido"), request);
        assertEquals(HttpStatus.CONFLICT, res.getStatusCode());
    }

    @Test
    void pagoYaProcesadoMapsTo409() {
        stubPath();
        ResponseEntity<ErrorResponse> res = handler.handlePagoYaProcesado(
            new PagoYaProcesadoException("MP-1"), request);
        assertEquals(HttpStatus.CONFLICT, res.getStatusCode());
    }

    @Test
    void securityMapsTo403() {
        stubPath();
        ResponseEntity<ErrorResponse> res = handler.handleSecurity(
            new SecurityException("No autorizado"), request);
        assertEquals(HttpStatus.FORBIDDEN, res.getStatusCode());
    }

    @Test
    void illegalStateMapsTo400() {
        stubPath();
        ResponseEntity<ErrorResponse> res = handler.handleIllegalState(
            new IllegalStateException("Solo se reembolsan pagos aprobados"), request);
        assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());
    }

    @Test
    void illegalArgumentMapsTo400() {
        stubPath();
        ResponseEntity<ErrorResponse> res = handler.handleIllegalArgument(
            new IllegalArgumentException("Tipo de suscriptor distinto"), request);
        assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());
    }

    @Test
    void missingHeaderMapsTo400() {
        stubPath();
        ResponseEntity<ErrorResponse> res = handler.handleMissingHeader(
            new MissingRequestHeaderException("X-User-Id", null), request);
        assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());
        assertTrue(res.getBody().message().contains("X-User-Id"));
    }

    @Test
    void missingParameterMapsTo400() {
        stubPath();
        ResponseEntity<ErrorResponse> res = handler.handleMissingParameter(
            new MissingServletRequestParameterException("monto", "String"), request);
        assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());
        assertTrue(res.getBody().message().contains("monto"));
    }

    @Test
    void typeMismatchMapsTo400() {
        stubPath();
        ResponseEntity<ErrorResponse> res = handler.handleTypeMismatch(
            new MethodArgumentTypeMismatchException("abc", Long.class, "id", null, null), request);
        assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());
    }

    @Test
    void unreadableBodyMapsTo400() {
        stubPath();
        ResponseEntity<ErrorResponse> res = handler.handleUnreadable(
            new HttpMessageNotReadableException("Mal JSON"), request);
        assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());
    }

    @Test
    void genericExceptionMapsTo500() {
        stubPath();
        ResponseEntity<ErrorResponse> res = handler.handleGeneric(
            new RuntimeException("NullPointerException"), request);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, res.getStatusCode());
    }
}