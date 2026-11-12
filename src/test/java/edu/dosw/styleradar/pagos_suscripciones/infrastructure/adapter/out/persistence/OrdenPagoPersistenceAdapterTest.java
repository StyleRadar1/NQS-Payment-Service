package edu.dosw.styleradar.pagos_suscripciones.infrastructure.adapter.out.persistence;

import edu.dosw.styleradar.pagos_suscripciones.domain.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@Import(OrdenPagoPersistenceAdapter.class)
class OrdenPagoPersistenceAdapterTest {

    @Autowired private OrdenPagoPersistenceAdapter adapter;

    @Test
    void debeGuardarYBuscarPorIdExterno() {
        OrdenPago o = new OrdenPago(null, 100L, new Monto(BigDecimal.valueOf(5000), "COP"), "MP-TEST-1");
        adapter.guardar(o);

        assertTrue(adapter.existePorIdPagoExterno("MP-TEST-1"));
        assertTrue(adapter.buscarPorIdPagoExterno("MP-TEST-1").isPresent());
    }

    @Test
    void debeBuscarPorSuscripcionId() {
        OrdenPago o = new OrdenPago(null, 100L, new Monto(BigDecimal.valueOf(5000), "COP"), "MP-TEST-2");
        OrdenPago guardada = adapter.guardar(o);

        assertTrue(adapter.buscarPorId(guardada.getId()).isPresent());
        assertEquals(1, adapter.buscarPorSuscripcionId(100L).size());
    }

    @Test
    void debePreservarMontoYMotivoDeReembolsoEnRehidratacion() {
        // Fix B4: antes, al reeler, montoReembolsado volvía a 0
        OrdenPago o = new OrdenPago(null, 100L, new Monto(new BigDecimal("100"), "COP"), "MP-REF-1");
        o.aprobar();
        o.reembolsar(new Monto(new BigDecimal("40"), "COP"), "parcial");
        OrdenPago guardada = adapter.guardar(o);

        OrdenPago recargada = adapter.buscarPorId(guardada.getId()).orElseThrow();
        assertEquals(0, recargada.getMontoReembolsado().valor().compareTo(new BigDecimal("40")));
        assertEquals("parcial", recargada.getMotivoReembolso());
        assertEquals(EstadoPago.APROBADO, recargada.getEstado()); // parcial → APROBADO
    }

    @Test
    void reembolsoTotalSePersisteComoReembolsado() {
        OrdenPago o = new OrdenPago(null, 100L, new Monto(new BigDecimal("100"), "COP"), "MP-REF-2");
        o.aprobar();
        o.reembolsar(new Monto(new BigDecimal("100"), "COP"), "total");
        OrdenPago guardada = adapter.guardar(o);

        OrdenPago recargada = adapter.buscarPorId(guardada.getId()).orElseThrow();
        assertEquals(EstadoPago.REEMBOLSADO, recargada.getEstado());
        assertTrue(recargada.fueReembolsadoTotalmente());
    }

    @Test
    void debeBuscarPorEstadoYBuscarTodos() {
        OrdenPago aprobada = new OrdenPago(null, 100L, new Monto(BigDecimal.TEN, "COP"), "MP-A-1");
        aprobada.aprobar();
        adapter.guardar(aprobada);

        OrdenPago pendiente = new OrdenPago(null, 100L, new Monto(BigDecimal.ONE, "COP"), "MP-P-1");
        adapter.guardar(pendiente);

        assertEquals(1, adapter.buscarPorEstado(EstadoPago.APROBADO).size());
        assertEquals(1, adapter.buscarPorEstado(EstadoPago.PENDIENTE).size());
        assertEquals(2, adapter.buscarTodos().size());
    }

    @Test
void debeBuscarPorEstadoAprobadoYRechazado() {
    OrdenPago aprobada = new OrdenPago(null, 100L, new Monto(BigDecimal.TEN, "COP"), "MP-A");
    aprobada.aprobar();
    adapter.guardar(aprobada);

    OrdenPago rechazada = new OrdenPago(null, 100L, new Monto(BigDecimal.ONE, "COP"), "MP-R");
    rechazada.rechazar();
    adapter.guardar(rechazada);

    assertEquals(1, adapter.buscarPorEstado(EstadoPago.APROBADO).size());
    assertEquals(1, adapter.buscarPorEstado(EstadoPago.RECHAZADO).size());
}

@Test
void debeBuscarPendientesAntesDeFecha() {
    OrdenPago o = new OrdenPago(null, 100L, new Monto(BigDecimal.TEN, "COP"), "MP-PEND");
    adapter.guardar(o);

    List<OrdenPago> result = adapter.buscarPendientesAntesDe(LocalDateTime.now().plusMinutes(1));
    assertEquals(1, result.size());
}

@Test
void debeRetornarVacioSiNoExistePorIdExterno() {
    assertTrue(adapter.buscarPorIdPagoExterno("NO-EXISTE").isEmpty());
    assertFalse(adapter.existePorIdPagoExterno("NO-EXISTE"));
}

@Test
void debeRetornarListaVaciaSiSuscripcionSinOrdenes() {
    assertTrue(adapter.buscarPorSuscripcionId(999L).isEmpty());
}
}