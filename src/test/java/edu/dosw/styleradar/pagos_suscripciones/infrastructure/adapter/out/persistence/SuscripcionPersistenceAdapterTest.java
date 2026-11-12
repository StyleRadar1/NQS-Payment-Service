package edu.dosw.styleradar.pagos_suscripciones.infrastructure.adapter.out.persistence;

import edu.dosw.styleradar.pagos_suscripciones.domain.model.*;
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

    @Autowired private SuscripcionPersistenceAdapter adapter;

    @Test
    void debeGuardarYActualizarSuscripcion() {
        Suscripcion s = new Suscripcion(null, "USER-999", TipoSuscriptor.USUARIO, 1L);
        s.setPreapprovalIdMp("PRE-999");

        Suscripcion guardada = adapter.guardar(s);
        guardada.activar(LocalDate.now().plusMonths(1));
        guardada.cancelarRenovacion();
        adapter.guardar(guardada);

        Optional<Suscripcion> encontrada = adapter.buscarPorSuscriptorId("USER-999");
        assertTrue(encontrada.isPresent());
        assertFalse(encontrada.get().isAutoRenovacion());
        assertNotNull(encontrada.get().getFechaFin());
    }

    @Test
    void suscripcionSuspendidaSeRecuperaComoSuspendidaNoActiva() {
        // Fix B1: antes cualquier suscripción con fechaFin se leía como ACTIVA
        Suscripcion s = new Suscripcion(null, "USER-SUSP", TipoSuscriptor.ALMACEN, 3L);
        s.activar(LocalDate.now().minusDays(1));
        s.marcarVencida();
        s.suspenderPorFaltaDePago();
        adapter.guardar(s);

        Suscripcion recargada = adapter.buscarPorSuscriptorId("USER-SUSP").orElseThrow();
        assertEquals(EstadoSuscripcion.SUSPENDIDA, recargada.getEstado());
        assertFalse(recargada.isAutoRenovacion());
    }

    @Test
    void debeConservarFechaInicioOriginalAlReleer() {
        Suscripcion s = new Suscripcion(null, "USER-FI", TipoSuscriptor.USUARIO, 2L);
        LocalDate inicioOriginal = LocalDate.now().minusDays(20);
        s.rehidratar(EstadoSuscripcion.ACTIVA, inicioOriginal,
                     LocalDate.now().plusDays(10), true, null, null);
        adapter.guardar(s);

        Suscripcion recargada = adapter.buscarPorSuscriptorId("USER-FI").orElseThrow();
        assertEquals(inicioOriginal, recargada.getFechaInicio());
    }

    @Test
    void debeRestaurarPlanProgramadoId() {
        Suscripcion s = new Suscripcion(null, "USER-PP", TipoSuscriptor.USUARIO, 2L);
        s.activar(LocalDate.now().plusMonths(1));
        s.programarCambioPlan(3L);
        adapter.guardar(s);

        Suscripcion recargada = adapter.buscarPorSuscriptorId("USER-PP").orElseThrow();
        assertEquals(3L, recargada.getPlanProgramadoId());
        assertEquals(2L, recargada.getPlanId()); // aún no se aplica
    }
}