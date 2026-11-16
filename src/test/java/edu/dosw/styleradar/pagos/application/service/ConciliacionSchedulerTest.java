package edu.dosw.styleradar.pagos.application.service;

import edu.dosw.styleradar.pagos.TestFixtures;
import edu.dosw.styleradar.pagos.application.port.out.EventPublisherPort;
import edu.dosw.styleradar.pagos.application.port.out.MercadoPagoPort;
import edu.dosw.styleradar.pagos.application.port.out.OrdenPagoRepositoryPort;
import edu.dosw.styleradar.pagos.application.port.out.SuscripcionRepositoryPort;
import edu.dosw.styleradar.pagos.domain.event.PagoConfirmadoEvent;
import edu.dosw.styleradar.pagos.domain.model.EstadoPago;
import edu.dosw.styleradar.pagos.domain.model.OrdenPago;
import edu.dosw.styleradar.pagos.domain.model.Suscripcion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ConciliacionScheduler (SR-RNF-14)")
class ConciliacionSchedulerTest {

    @Mock
    private OrdenPagoRepositoryPort ordenPagoRepository;
    @Mock
    private SuscripcionRepositoryPort suscripcionRepository;
    @Mock
    private MercadoPagoPort mercadoPago;
    @Mock
    private SuscripcionActivacionService activacionService;
    @Mock
    private EventPublisherPort eventPublisher;

    private ConciliacionScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new ConciliacionScheduler(ordenPagoRepository, suscripcionRepository,
                mercadoPago, activacionService, eventPublisher, TestFixtures.CLOCK);
    }

    private OrdenPago pendienteConPago() {
        OrdenPago orden = TestFixtures.conIdOrden(TestFixtures.pendiente(100L, "14900"), 10L);
        orden.actualizarEstado(EstadoPago.PENDIENTE, "MP-1");
        return orden;
    }

    @Test
    void corrigeEstadoAprobadoYActiva() {
        OrdenPago orden = pendienteConPago();
        Suscripcion suscripcion = TestFixtures.activaUsuario("USER-1",
                TestFixtures.HOY, TestFixtures.HOY.plusMonths(1));
        when(ordenPagoRepository.buscarPendientesAntesDe(any())).thenReturn(List.of(orden));
        when(mercadoPago.consultarPago("MP-1")).thenReturn(new MercadoPagoPort.PagoMp(
                "MP-1", "approved", null, null, new BigDecimal("14900.00"), "COP"));
        when(ordenPagoRepository.guardar(any(OrdenPago.class))).thenAnswer(inv -> inv.getArgument(0));
        when(suscripcionRepository.buscarPorId(100L)).thenReturn(Optional.of(suscripcion));
        when(activacionService.activar(any(), any(), any())).thenReturn(suscripcion);

        scheduler.conciliarPagosPendientes();

        assertThat(orden.getEstado()).isEqualTo(EstadoPago.APROBADO);
        verify(activacionService).activar(any(), any(), any());
        verify(eventPublisher).publicarPagoConfirmado(any(PagoConfirmadoEvent.class));
    }

    @Test
    void omiteOrdenSinPagoExterno() {
        OrdenPago sinPago = TestFixtures.conIdOrden(TestFixtures.pendiente(100L, "14900"), 11L);
        when(ordenPagoRepository.buscarPendientesAntesDe(any())).thenReturn(List.of(sinPago));

        scheduler.conciliarPagosPendientes();

        verify(mercadoPago, never()).consultarPago(anyString());
    }

    @Test
    void omiteSiElEstadoNoCambioOEsDesconocido() {
        OrdenPago orden = pendienteConPago();
        when(ordenPagoRepository.buscarPendientesAntesDe(any())).thenReturn(List.of(orden));
        when(mercadoPago.consultarPago("MP-1"))
                .thenReturn(new MercadoPagoPort.PagoMp("MP-1", "pending", null, null, null, null))
                .thenReturn(new MercadoPagoPort.PagoMp("MP-1", "raro", null, null, null, null));

        scheduler.conciliarPagosPendientes();
        scheduler.conciliarPagosPendientes();

        verify(ordenPagoRepository, never()).guardar(any());
    }

    @Test
    void omiteSiMercadoPagoNoDevuelveElPago() {
        when(ordenPagoRepository.buscarPendientesAntesDe(any())).thenReturn(List.of(pendienteConPago()));
        when(mercadoPago.consultarPago("MP-1")).thenReturn(null);

        scheduler.conciliarPagosPendientes();

        verify(ordenPagoRepository, never()).guardar(any());
    }

    @Test
    void continuaConLasDemasSiUnaFalla() {
        OrdenPago primera = pendienteConPago();
        OrdenPago segunda = TestFixtures.conIdOrden(TestFixtures.pendiente(101L, "14900"), 20L);
        segunda.actualizarEstado(EstadoPago.PENDIENTE, "MP-2");
        when(ordenPagoRepository.buscarPendientesAntesDe(any())).thenReturn(List.of(primera, segunda));
        when(mercadoPago.consultarPago("MP-1")).thenThrow(new IllegalStateException("MP"));
        when(mercadoPago.consultarPago("MP-2")).thenReturn(new MercadoPagoPort.PagoMp(
                "MP-2", "rejected", null, null, null, null));
        when(ordenPagoRepository.guardar(any(OrdenPago.class))).thenAnswer(inv -> inv.getArgument(0));

        scheduler.conciliarPagosPendientes();

        assertThat(segunda.getEstado()).isEqualTo(EstadoPago.RECHAZADO);
    }

    @Test
    void capturaFalloAlActivar() {
        OrdenPago orden = pendienteConPago();
        when(ordenPagoRepository.buscarPendientesAntesDe(any())).thenReturn(List.of(orden));
        when(mercadoPago.consultarPago("MP-1")).thenReturn(new MercadoPagoPort.PagoMp(
                "MP-1", "approved", null, null, null, null));
        when(ordenPagoRepository.guardar(any(OrdenPago.class))).thenAnswer(inv -> inv.getArgument(0));
        when(suscripcionRepository.buscarPorId(100L)).thenReturn(Optional.empty());

        scheduler.conciliarPagosPendientes();

        verify(eventPublisher, never()).publicarPagoConfirmado(any());
    }
}
