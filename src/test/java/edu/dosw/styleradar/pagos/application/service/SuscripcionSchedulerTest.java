package edu.dosw.styleradar.pagos.application.service;

import edu.dosw.styleradar.pagos.TestFixtures;
import edu.dosw.styleradar.pagos.application.port.out.EventPublisherPort;
import edu.dosw.styleradar.pagos.application.port.out.SuscripcionRepositoryPort;
import edu.dosw.styleradar.pagos.domain.event.SuscripcionSuspendidaEvent;
import edu.dosw.styleradar.pagos.domain.model.EstadoSuscripcion;
import edu.dosw.styleradar.pagos.domain.model.Suscripcion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("SuscripcionScheduler (SR-RF-99/103)")
class SuscripcionSchedulerTest {

    @Mock
    private SuscripcionRepositoryPort suscripcionRepository;
    @Mock
    private EventPublisherPort eventPublisher;

    private SuscripcionScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new SuscripcionScheduler(suscripcionRepository, eventPublisher, TestFixtures.CLOCK);
    }

    @Test
    void suspendeVencidasYPublicaEvento() {
        Suscripcion vencida = TestFixtures.activaAlmacen("ALMACEN-1", "900123",
                TestFixtures.HOY.minusMonths(1), TestFixtures.HOY.minusDays(1));
        when(suscripcionRepository.buscarActivasVencidas(TestFixtures.HOY)).thenReturn(List.of(vencida));
        when(suscripcionRepository.guardar(any(Suscripcion.class))).thenAnswer(inv -> inv.getArgument(0));

        scheduler.revisarSuscripcionesVencidas();

        assertThat(vencida.getEstado()).isEqualTo(EstadoSuscripcion.SUSPENDIDA);
        assertThat(vencida.isAutoRenovacion()).isFalse();
        ArgumentCaptor<SuscripcionSuspendidaEvent> evento =
                ArgumentCaptor.forClass(SuscripcionSuspendidaEvent.class);
        verify(eventPublisher).publicarSuscripcionSuspendida(evento.capture());
        assertThat(evento.getValue().data().suscriptorId()).isEqualTo("ALMACEN-1");
        assertThat(evento.getValue().data().nit()).isEqualTo("900123");
        assertThat(evento.getValue().data().motivo()).isEqualTo("FALTA_DE_PAGO");
    }

    @Test
    void noHaceNadaSiNoHayVencidas() {
        when(suscripcionRepository.buscarActivasVencidas(TestFixtures.HOY)).thenReturn(List.of());

        scheduler.revisarSuscripcionesVencidas();

        verify(suscripcionRepository, never()).guardar(any());
        verify(eventPublisher, never()).publicarSuscripcionSuspendida(any());
    }

    @Test
    void continuaConLasDemasSiUnaFalla() {
        Suscripcion primera = TestFixtures.activaUsuario("USER-1",
                TestFixtures.HOY.minusMonths(1), TestFixtures.HOY.plusDays(1));
        Suscripcion segunda = TestFixtures.activaUsuario("USER-2",
                TestFixtures.HOY.minusMonths(1), TestFixtures.HOY.minusDays(1));
        when(suscripcionRepository.buscarActivasVencidas(TestFixtures.HOY))
                .thenReturn(List.of(primera, segunda));
        when(suscripcionRepository.guardar(any(Suscripcion.class))).thenAnswer(inv -> inv.getArgument(0));

        scheduler.revisarSuscripcionesVencidas();

        verify(suscripcionRepository, times(1)).guardar(segunda);
        verify(eventPublisher, times(1)).publicarSuscripcionSuspendida(any());
    }

    @Test
    void capturaFalloDelBrokerSinRomperElJob() {
        Suscripcion vencida = TestFixtures.activaUsuario("USER-1",
                TestFixtures.HOY.minusMonths(1), TestFixtures.HOY.minusDays(1));
        when(suscripcionRepository.buscarActivasVencidas(TestFixtures.HOY)).thenReturn(List.of(vencida));
        when(suscripcionRepository.guardar(any(Suscripcion.class))).thenAnswer(inv -> inv.getArgument(0));
        doThrow(new IllegalStateException("Rabbit caído"))
                .when(eventPublisher).publicarSuscripcionSuspendida(any());

        scheduler.revisarSuscripcionesVencidas();

        assertThat(vencida.getEstado()).isEqualTo(EstadoSuscripcion.SUSPENDIDA);
    }
}
