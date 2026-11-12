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
    void debeRegistrarVencidaYLuegoSuspender() {
        Suscripcion usuario = new Suscripcion(1L, "U1", TipoSuscriptor.USUARIO, 2L);
        Suscripcion almacen = new Suscripcion(2L, "A1", TipoSuscriptor.ALMACEN, 3L);

        when(suscripcionRepository.buscarVencidas(any(LocalDate.class)))
            .thenReturn(List.of(usuario, almacen));

        scheduler.revisarSuscripcionesVencidas();

        assertEquals(1L, usuario.getPlanId());               // degradado a gratuito
        assertEquals(EstadoSuscripcion.SUSPENDIDA, usuario.getEstado());
        assertEquals(EstadoSuscripcion.SUSPENDIDA, almacen.getEstado());

        // cada uno guarda dos veces (VENCIDA + SUSPENDIDA)
        verify(suscripcionRepository, times(4)).guardar(any());
        verify(eventPublisher, times(2)).publicarSuscripcionSuspendida(any());
    }
}