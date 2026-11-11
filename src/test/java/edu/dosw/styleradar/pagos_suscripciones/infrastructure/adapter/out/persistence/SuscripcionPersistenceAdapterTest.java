package edu.dosw.styleradar.pagos_suscripciones.infrastructure.adapter.out.persistence;

import edu.dosw.styleradar.pagos_suscripciones.domain.model.Suscripcion;
import edu.dosw.styleradar.pagos_suscripciones.domain.model.TipoSuscriptor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@Import(SuscripcionPersistenceAdapter.class)
class SuscripcionPersistenceAdapterTest {

    @Autowired
    private SuscripcionPersistenceAdapter adapter;

    @Test
    void debeGuardarYActualizarSuscripcion() {
        Suscripcion suscripcion = new Suscripcion(null, "USER-999", TipoSuscriptor.USUARIO, 1L);
        suscripcion.setPreapprovalIdMp("PRE-999");
        
        Suscripcion guardada = adapter.guardar(suscripcion);
        
        // Actualizamos para cubrir el mapeo completo
        guardada.activar(LocalDate.now().plusMonths(1));
        guardada.cancelarRenovacion();
        adapter.guardar(guardada);

        Optional<Suscripcion> encontrada = adapter.buscarPorSuscriptorId("USER-999");
        assertTrue(encontrada.isPresent());
        assertFalse(encontrada.get().isAutoRenovacion());
        assertNotNull(encontrada.get().getFechaFin());
    }
}