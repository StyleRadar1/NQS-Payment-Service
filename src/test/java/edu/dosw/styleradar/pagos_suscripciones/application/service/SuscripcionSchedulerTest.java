package edu.dosw.styleradar.pagos_suscripciones.application.service;

import edu.dosw.styleradar.pagos_suscripciones.domain.model.*;
import edu.dosw.styleradar.pagos_suscripciones.domain.port.out.EventPublisherPort;
import edu.dosw.styleradar.pagos_suscripciones.domain.port.out.SuscripcionRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SuscripcionSchedulerTest {

    @Mock private SuscripcionRepositoryPort suscripcionRepository;
    @Mock private EventPublisherPort eventPublisher;
    @InjectMocks private SuscripcionScheduler scheduler;

    @Test
    void debeProcesarSuscripcionesVencidasCorrectamente() {
        Suscripcion userSub = new Suscripcion(1L, "USER-1", TipoSuscriptor.USUARIO, 2L);
        Suscripcion storeSub = new Suscripcion(2L, "STORE-1", TipoSuscriptor.ALMACEN, 3L);
        
        when(suscripcionRepository.buscarVencidas(any(LocalDate.class)))
                .thenReturn(List.of(userSub, storeSub));

        scheduler.revisarSuscripcionesVencidas();

        // Usuario pasa a plan gratis (ID 1) y se suspende
        assertEquals(1L, userSub.getPlanId());
        assertEquals(EstadoSuscripcion.SUSPENDIDA, userSub.getEstado());
        
        // Almacén se suspende
        assertEquals(EstadoSuscripcion.SUSPENDIDA, storeSub.getEstado());

        verify(suscripcionRepository, times(2)).guardar(any());
        verify(eventPublisher, times(2)).publicarSuscripcionSuspendida(any());
    }
}