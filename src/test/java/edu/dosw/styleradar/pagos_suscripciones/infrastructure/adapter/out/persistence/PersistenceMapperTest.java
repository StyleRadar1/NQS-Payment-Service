package edu.dosw.styleradar.pagos_suscripciones.infrastructure.adapter.out.persistence;

import edu.dosw.styleradar.pagos_suscripciones.domain.model.*;
import edu.dosw.styleradar.pagos_suscripciones.infrastructure.adapter.out.persistence.entity.SuscripcionEntity;
import edu.dosw.styleradar.pagos_suscripciones.infrastructure.adapter.out.persistence.repository.SuscripcionJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PersistenceMapperTest {

    @Mock private SuscripcionJpaRepository repository;
    @InjectMocks private SuscripcionPersistenceAdapter adapter;

    @Test
    void suscripcionSuspendidaConFechaNoSeRehidrataComoActiva() {
        SuscripcionEntity e = new SuscripcionEntity();
        e.setId(1L); e.setSuscriptorId("U"); e.setTipoSuscriptor(TipoSuscriptor.USUARIO);
        e.setPlanId(2L); e.setEstado(EstadoSuscripcion.SUSPENDIDA);
        e.setFechaInicio(LocalDate.now().minusDays(30));
        e.setFechaFin(LocalDate.now().minusDays(1));
        e.setAutoRenovacion(false);
        e.setPlanProgramadoId(null);

        when(repository.findBySuscriptorId("U")).thenReturn(Optional.of(e));

        Suscripcion d = adapter.buscarPorSuscriptorId("U").orElseThrow();
        assertEquals(EstadoSuscripcion.SUSPENDIDA, d.getEstado());
        assertEquals(e.getFechaInicio(), d.getFechaInicio()); // no reinició
        assertFalse(d.isAutoRenovacion());
    }

    @Test
    void restauraPlanProgramadoId() {
        SuscripcionEntity e = new SuscripcionEntity();
        e.setId(1L); e.setSuscriptorId("U"); e.setTipoSuscriptor(TipoSuscriptor.USUARIO);
        e.setPlanId(2L); e.setPlanProgramadoId(1L);
        e.setEstado(EstadoSuscripcion.ACTIVA); e.setAutoRenovacion(true);

        when(repository.findBySuscriptorId("U")).thenReturn(Optional.of(e));
        Suscripcion d = adapter.buscarPorSuscriptorId("U").orElseThrow();
        assertEquals(1L, d.getPlanProgramadoId());
    }
}