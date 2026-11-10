package edu.dosw.styleradar.pagos_suscripciones.domain.model;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class OrdenPagoTest {

    @Test
    void debeCrearOrdenPendientePorDefecto() {
        OrdenPago orden = new OrdenPago(1L, 1L, new Monto(BigDecimal.TEN, "COP"), "MP-1");
        assertEquals(EstadoPago.PENDIENTE, orden.getEstado());
        assertEquals(BigDecimal.ZERO, orden.getMontoReembolsado().valor());
    }

    @Test
    void debeActualizarEstadoDesdeString() {
        OrdenPago orden = new OrdenPago(1L, 1L, new Monto(BigDecimal.TEN, "COP"), "MP-1");
        orden.actualizarEstado("aprobado"); // Minúsculas para probar el toUpperCase()
        assertEquals(EstadoPago.APROBADO, orden.getEstado());
    }

    @Test
    void debeAprobarPagoPendiente() {
        OrdenPago orden = new OrdenPago(1L, 1L, new Monto(BigDecimal.TEN, "COP"), "MP-1");
        orden.aprobar();
        assertEquals(EstadoPago.APROBADO, orden.getEstado());
    }

    @Test
    void noDebeAprobarPagoYaAprobado() {
        OrdenPago orden = new OrdenPago(1L, 1L, new Monto(BigDecimal.TEN, "COP"), "MP-1");
        orden.aprobar();
        assertThrows(IllegalStateException.class, orden::aprobar);
    }

    @Test
    void debeRechazarPago() {
        OrdenPago orden = new OrdenPago(1L, 1L, new Monto(BigDecimal.TEN, "COP"), "MP-1");
        orden.rechazar();
        assertEquals(EstadoPago.RECHAZADO, orden.getEstado());
    }

    @Test
    void debeReembolsarPagoAprobado() {
        OrdenPago orden = new OrdenPago(1L, 1L, new Monto(new BigDecimal("100"), "COP"), "MP-1");
        orden.aprobar();
        
        Monto reembolso = new Monto(new BigDecimal("50"), "COP");
        orden.reembolsar(reembolso, "Motivo test");
        
        assertEquals(EstadoPago.REEMBOLSADO, orden.getEstado());
        assertEquals(new BigDecimal("50"), orden.getMontoReembolsado().valor());
        assertEquals("Motivo test", orden.getMotivoReembolso());
    }

    @Test
    void noDebeReembolsarPagoNoAprobado() {
        OrdenPago orden = new OrdenPago(1L, 1L, new Monto(new BigDecimal("100"), "COP"), "MP-1");
        Monto reembolso = new Monto(new BigDecimal("50"), "COP");
        
        assertThrows(IllegalStateException.class, () -> orden.reembolsar(reembolso, "Motivo"));
    }

    @Test
    void noDebeReembolsarMasDelMontoOriginal() {
        OrdenPago orden = new OrdenPago(1L, 1L, new Monto(new BigDecimal("100"), "COP"), "MP-1");
        orden.aprobar();
        Monto reembolsoExcesivo = new Monto(new BigDecimal("150"), "COP");
        
        assertThrows(IllegalArgumentException.class, () -> orden.reembolsar(reembolsoExcesivo, "Motivo"));
    }
}