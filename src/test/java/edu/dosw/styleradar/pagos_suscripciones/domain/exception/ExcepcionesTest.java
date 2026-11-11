package edu.dosw.styleradar.pagos_suscripciones.domain.exception;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ExcepcionesTest {

    @Test
    void debeCrearIdempotencyException() {
        IdempotencyException ex = new IdempotencyException("Error de idempotencia");
        assertEquals("Error de idempotencia", ex.getMessage());
    }

    @Test
    void debeCrearPagoYaProcesadoException() {
        PagoYaProcesadoException ex = new PagoYaProcesadoException("MP-123");
        assertEquals("El pago con ID externo MP-123 ya fue procesado (Idempotencia)", ex.getMessage());
    }

    @Test
    void debeCrearPaymentProcessingException() {
        PaymentProcessingException ex = new PaymentProcessingException("Fallo en el pago");
        assertEquals("Fallo en el pago", ex.getMessage());
    }

    @Test
    void debeCrearPlanNotFoundException() {
        PlanNotFoundException ex = new PlanNotFoundException("Plan no encontrado");
        assertEquals("Plan no encontrado", ex.getMessage());
    }

    @Test
    void debeCrearSuscripcionNoEncontradaExceptionPorId() {
        SuscripcionNoEncontradaException ex = new SuscripcionNoEncontradaException(100L);
        assertEquals("No se encontró la suscripción con ID: 100", ex.getMessage());
    }

    @Test
    void debeCrearSuscripcionNoEncontradaExceptionPorMensaje() {
        SuscripcionNoEncontradaException ex = new SuscripcionNoEncontradaException("Mensaje custom");
        assertEquals("Mensaje custom", ex.getMessage());
    }
}