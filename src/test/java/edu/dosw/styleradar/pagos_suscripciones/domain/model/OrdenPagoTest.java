package edu.dosw.styleradar.pagos_suscripciones.domain.model;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class OrdenPagoTest {

    @Test
    void debeCrearOrdenPendientePorDefecto() {
        OrdenPago orden = new OrdenPago(1L, 1L, new Monto(BigDecimal.TEN, "COP"), "MP-1");
        assertEquals(EstadoPago.PENDIENTE, orden.getEstado());
        assertEquals(0, orden.getMontoReembolsado().valor().compareTo(BigDecimal.ZERO));
    }

    @Test
    void debeActualizarEstadoDesdeString() {
        OrdenPago orden = new OrdenPago(1L, 1L, new Monto(BigDecimal.TEN, "COP"), "MP-1");
        orden.actualizarEstado("aprobado");
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
    void reembolsoParcialMantieneEstadoAprobado() {
        OrdenPago orden = new OrdenPago(1L, 1L, new Monto(new BigDecimal("100"), "COP"), "MP-1");
        orden.aprobar();

        orden.reembolsar(new Monto(new BigDecimal("50"), "COP"), "parcial");

        assertEquals(EstadoPago.APROBADO, orden.getEstado());
        assertEquals(0, orden.getMontoReembolsado().valor().compareTo(new BigDecimal("50")));
        assertEquals("parcial", orden.getMotivoReembolso());
        assertFalse(orden.fueReembolsadoTotalmente());
    }

    @Test
    void reembolsoTotalCambiaEstadoAReembolsado() {
        OrdenPago orden = new OrdenPago(1L, 1L, new Monto(new BigDecimal("100"), "COP"), "MP-1");
        orden.aprobar();

        orden.reembolsar(new Monto(new BigDecimal("100"), "COP"), "total");

        assertEquals(EstadoPago.REEMBOLSADO, orden.getEstado());
        assertTrue(orden.fueReembolsadoTotalmente());
    }

    @Test
    void reembolsoAcumuladoHastaTotalCambiaEstado() {
        OrdenPago orden = new OrdenPago(1L, 1L, new Monto(new BigDecimal("100"), "COP"), "MP-1");
        orden.aprobar();

        orden.reembolsar(new Monto(new BigDecimal("40"), "COP"), "1");
        assertEquals(EstadoPago.APROBADO, orden.getEstado());

        orden.reembolsar(new Monto(new BigDecimal("60"), "COP"), "2");
        assertEquals(EstadoPago.REEMBOLSADO, orden.getEstado());
    }

    @Test
    void noDebeReembolsarPagoNoAprobado() {
        OrdenPago orden = new OrdenPago(1L, 1L, new Monto(new BigDecimal("100"), "COP"), "MP-1");
        Monto reembolso = new Monto(new BigDecimal("50"), "COP");
        assertThrows(IllegalStateException.class, () -> orden.reembolsar(reembolso, "motivo"));
    }

    @Test
    void noDebeReembolsarMasDelMontoOriginal() {
        OrdenPago orden = new OrdenPago(1L, 1L, new Monto(new BigDecimal("100"), "COP"), "MP-1");
        orden.aprobar();
        Monto exceso = new Monto(new BigDecimal("150"), "COP");
        assertThrows(IllegalArgumentException.class, () -> orden.reembolsar(exceso, "m"));
    }

    @Test
    void rehidratarRestauraEstadoYReembolso() {
        OrdenPago orden = new OrdenPago(1L, 1L, new Monto(new BigDecimal("100"), "COP"), "MP-1");

        orden.rehidratar(EstadoPago.REEMBOLSADO, new Monto(new BigDecimal("100"), "COP"), "rehidratado");

        assertEquals(EstadoPago.REEMBOLSADO, orden.getEstado());
        assertEquals(0, orden.getMontoReembolsado().valor().compareTo(new BigDecimal("100")));
        assertEquals("rehidratado", orden.getMotivoReembolso());
    }

    @Test
    void rehidratarNoSobrescribeSiMontoEsNulo() {
        OrdenPago orden = new OrdenPago(1L, 1L, new Monto(new BigDecimal("100"), "COP"), "MP-1");

        orden.rehidratar(EstadoPago.APROBADO, null, null);

        assertEquals(EstadoPago.APROBADO, orden.getEstado());
        assertEquals(0, orden.getMontoReembolsado().valor().compareTo(BigDecimal.ZERO));
    }
    @Test
void reembolsarSinMontoPrevioDebeInicializarEnCero() {
    OrdenPago o = new OrdenPago(1L, 1L, new Monto(new BigDecimal("100"), "COP"), "MP-1");
    o.aprobar();
    // Forzar rehidratación sin montoReembolsado previo para cubrir la rama null
    o.rehidratar(EstadoPago.APROBADO, null, null);
    o.reembolsar(new Monto(new BigDecimal("30"), "COP"), "p1");
    assertEquals(0, o.getMontoReembolsado().valor().compareTo(new BigDecimal("30")));
}

@Test
void actualizarEstadoConMinusculasYMayusculas() {
    OrdenPago o = new OrdenPago(1L, 1L, new Monto(BigDecimal.TEN, "COP"), "MP-1");
    o.actualizarEstado("Reembolsado");
    assertEquals(EstadoPago.REEMBOLSADO, o.getEstado());
}
}