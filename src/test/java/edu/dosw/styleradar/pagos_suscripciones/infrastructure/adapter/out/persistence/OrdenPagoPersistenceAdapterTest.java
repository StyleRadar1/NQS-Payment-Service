package edu.dosw.styleradar.pagos_suscripciones.infrastructure.adapter.out.persistence;

import edu.dosw.styleradar.pagos_suscripciones.domain.model.Monto;
import edu.dosw.styleradar.pagos_suscripciones.domain.model.OrdenPago;
import edu.dosw.styleradar.pagos_suscripciones.infrastructure.adapter.out.persistence.repository.OrdenPagoJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@Import(OrdenPagoPersistenceAdapter.class)
class OrdenPagoPersistenceAdapterTest {

    @Autowired
    private OrdenPagoPersistenceAdapter adapter;

    @Test
    void debeGuardarYBuscarPorIdExterno() {
        OrdenPago orden = new OrdenPago(null, 100L, new Monto(BigDecimal.valueOf(5000), "COP"), "MP-TEST-1");
        
        adapter.guardar(orden);
        
        assertTrue(adapter.existePorIdPagoExterno("MP-TEST-1"));
        assertTrue(adapter.buscarPorIdPagoExterno("MP-TEST-1").isPresent());
    }

    @Test
    void debeBuscarOrdenPorIdYPorSuscripcion() {
        OrdenPago orden = new OrdenPago(null, 100L, new Monto(BigDecimal.valueOf(5000), "COP"), "MP-TEST-2");
        OrdenPago guardada = adapter.guardar(orden);
        
        assertTrue(adapter.buscarPorId(guardada.getId()).isPresent());
        assertEquals(1, adapter.buscarPorSuscripcionId(100L).size());
    }
}