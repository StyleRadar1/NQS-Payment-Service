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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PersistenceMapperTest {

    @Mock
    private SuscripcionJpaRepository repository;

    @InjectMocks
    private SuscripcionPersistenceAdapter adapter;

    @Test
    void debeMapearSuscripcionConFechasYAutoRenovacionDesactivada() {
        SuscripcionEntity entity = new SuscripcionEntity();
        entity.setId(1L);
        entity.setSuscriptorId("USER-1");
        entity.setTipoSuscriptor(TipoSuscriptor.USUARIO);
        entity.setPlanId(2L);
        entity.setEstado(EstadoSuscripcion.ACTIVA);
        entity.setFechaFin(LocalDate.now().plusDays(10));
        entity.setAutoRenovacion(false);
        
        when(repository.findBySuscriptorId("USER-1")).thenReturn(Optional.of(entity));

        Suscripcion domain = adapter.buscarPorSuscriptorId("USER-1").orElseThrow();

        assertFalse(domain.isAutoRenovacion());
        assertNotNull(domain.getFechaFin());
    }

    @Test
    void debeMapearSuscripcionSinFechaFin() {
        SuscripcionEntity entity = new SuscripcionEntity();
        entity.setId(1L);
        entity.setSuscriptorId("USER-2");
        entity.setTipoSuscriptor(TipoSuscriptor.USUARIO);
        entity.setPlanId(1L);
        entity.setEstado(EstadoSuscripcion.PENDIENTE);
        entity.setFechaFin(null); // Cubre el else
        
        when(repository.findBySuscriptorId("USER-2")).thenReturn(Optional.of(entity));

        Suscripcion domain = adapter.buscarPorSuscriptorId("USER-2").orElseThrow();
        assertEquals(EstadoSuscripcion.PENDIENTE, domain.getEstado());
    }
}