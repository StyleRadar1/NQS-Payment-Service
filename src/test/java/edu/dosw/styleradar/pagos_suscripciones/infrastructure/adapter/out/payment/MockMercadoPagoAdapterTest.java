package edu.dosw.styleradar.pagos_suscripciones.infrastructure.adapter.out.payment;

import edu.dosw.styleradar.pagos_suscripciones.domain.model.Monto;
import edu.dosw.styleradar.pagos_suscripciones.domain.model.OrdenPago;
import edu.dosw.styleradar.pagos_suscripciones.domain.model.Suscripcion;
import edu.dosw.styleradar.pagos_suscripciones.domain.model.TipoSuscriptor;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class MockMercadoPagoAdapterTest {

    private final MockMercadoPagoAdapter adapter = new MockMercadoPagoAdapter();

    @Test
    void debeSimularCreacionDePago() {
        OrdenPago orden = new OrdenPago(1L, 1L, new Monto(BigDecimal.TEN, "COP"), "MP-123");
        Suscripcion suscripcion = new Suscripcion(1L, "USER-1", TipoSuscriptor.USUARIO, 2L);

        Map<String, String> response = adapter.crearPreferenciaPago(orden, suscripcion, "test@test.com");

        assertTrue(response.get("init_point").contains("MOCK-MP-123"));
        assertTrue(response.get("preapproval_id").contains("MOCK-PREAPPROVAL-MP-123"));
    }

    @Test
    void debeSimularCancelacionYReembolso() {
        // Al no lanzar excepción y ejecutar las líneas del log, ya cubre el 100%
        assertDoesNotThrow(() -> {
            adapter.cancelarPreapproval("PRE-123");
            adapter.reembolsarPago("MP-123", "100.00");
        });
    }
}