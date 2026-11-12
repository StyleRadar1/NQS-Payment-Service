package edu.dosw.styleradar.pagos_suscripciones.infrastructure.adapter.out.payment;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class MockMercadoPagoAdapterTest {

    private final MockMercadoPagoAdapter adapter = new MockMercadoPagoAdapter();

    @Test
    void debeRetornarApproved() {
        assertEquals("approved", adapter.consultarEstadoPago("MP-X"));
    }
}