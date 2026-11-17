package edu.dosw.styleradar.pagos.application.usecase;

import edu.dosw.styleradar.pagos.TestFixtures;
import edu.dosw.styleradar.pagos.application.dto.request.NotificacionPago;
import edu.dosw.styleradar.pagos.application.port.out.EventPublisherPort;
import edu.dosw.styleradar.pagos.application.port.out.IdempotencyPort;
import edu.dosw.styleradar.pagos.application.port.out.MercadoPagoPort;
import edu.dosw.styleradar.pagos.application.port.out.OrdenPagoRepositoryPort;
import edu.dosw.styleradar.pagos.application.port.out.SuscripcionRepositoryPort;
import edu.dosw.styleradar.pagos.application.port.out.WebhookLogPort;
import edu.dosw.styleradar.pagos.application.service.SuscripcionActivacionService;
import edu.dosw.styleradar.pagos.domain.event.PagoConfirmadoEvent;
import edu.dosw.styleradar.pagos.domain.exception.IdempotencyException;
import edu.dosw.styleradar.pagos.domain.exception.PaymentProcessingException;
import edu.dosw.styleradar.pagos.domain.exception.SolicitudInvalidaException;
import edu.dosw.styleradar.pagos.domain.model.EstadoPago;
import edu.dosw.styleradar.pagos.domain.model.OrdenPago;
import edu.dosw.styleradar.pagos.domain.model.Suscripcion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("WebhookUseCase (SR-RF-94, SR-RNF-14)")
class WebhookUseCaseTest {

    @Mock
    private OrdenPagoRepositoryPort ordenPagoRepository;
    @Mock
    private SuscripcionRepositoryPort suscripcionRepository;
    @Mock
    private MercadoPagoPort mercadoPago;
    @Mock
    private IdempotencyPort idempotencyPort;
    @Mock
    private WebhookLogPort webhookLogPort;
    @Mock
    private SuscripcionActivacionService activacionService;
    @Mock
    private EventPublisherPort eventPublisher;

    private WebhookUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new WebhookUseCase(ordenPagoRepository, suscripcionRepository, mercadoPago,
                idempotencyPort, webhookLogPort, activacionService, eventPublisher, TestFixtures.CLOCK);
    }

    private NotificacionPago notificacion(String id) {
        return new NotificacionPago(id, "payment", "req-1", Map.of("type", "payment"));
    }

    private MercadoPagoPort.PagoMp pago(String estado, String referencia, String preapproval) {
        return new MercadoPagoPort.PagoMp("MP-1", estado, referencia, preapproval,
                new BigDecimal("14900.00"), "COP");
    }

    private void lockLibre() {
        when(idempotencyPort.intentarBloquear(anyString(), any(Duration.class))).thenReturn(true);
    }

    @Test
    void rechazaNotificacionSinPago() {
        assertThatThrownBy(() -> useCase.procesar(null, "corr"))
                .isInstanceOf(SolicitudInvalidaException.class);
        assertThatThrownBy(() -> useCase.procesar(new NotificacionPago(" ", "payment", null, Map.of()), "corr"))
                .isInstanceOf(SolicitudInvalidaException.class);
    }

    @Test
    void ignoraNotificacionesDePreapproval() {
        useCase.procesar(new NotificacionPago("MP-1", "subscription_preapproval", null, Map.of()), "corr");

        verifyNoInteractions(mercadoPago, idempotencyPort, webhookLogPort);
    }

    @Test
    void respetaElBloqueoDeIdempotencia() {
        when(idempotencyPort.intentarBloquear(eq("lock:pago_MP-1"), any(Duration.class)))
                .thenReturn(false);

        assertThatThrownBy(() -> useCase.procesar(notificacion("MP-1"), "corr"))
                .isInstanceOf(IdempotencyException.class);
        verify(mercadoPago, never()).consultarPago(anyString());
    }

    @Test
    void omiteWebhookDuplicadoYLiberaElLock() {
        lockLibre();
        when(webhookLogPort.existe("MP-1")).thenReturn(true);

        useCase.procesar(notificacion("MP-1"), "corr");

        verify(mercadoPago, never()).consultarPago(anyString());
        verify(idempotencyPort).liberar("lock:pago_MP-1");
    }

    @Test
    void omitePagoNoAsociadoAStyleRadar() {
        lockLibre();
        when(webhookLogPort.existe("MP-1")).thenReturn(false);
        when(mercadoPago.consultarPago("MP-1")).thenReturn(pago("approved", null, null));

        useCase.procesar(notificacion("MP-1"), "corr");

        verify(ordenPagoRepository, never()).guardar(any());
        verify(idempotencyPort).liberar("lock:pago_MP-1");
    }

    @Test
    void activaSuscripcionConPagoAprobadoDeUnaOrdenExistente() {
        lockLibre();
        OrdenPago pendiente = TestFixtures.conIdOrden(TestFixtures.pendiente(100L, "14900"), 10L);
        when(ordenPagoRepository.buscarPorIdPagoExterno("MP-1")).thenReturn(Optional.of(pendiente));
        when(mercadoPago.consultarPago("MP-1")).thenReturn(pago("approved", null, null));
        when(ordenPagoRepository.guardar(any(OrdenPago.class))).thenAnswer(inv -> inv.getArgument(0));
        Suscripcion activada = TestFixtures.activaUsuario("USER-1",
                TestFixtures.HOY, TestFixtures.HOY.plusMonths(1));
        when(activacionService.activar(any(), any(), any())).thenReturn(activada);
        when(suscripcionRepository.buscarPorId(100L)).thenReturn(Optional.of(activada));

        useCase.procesar(notificacion("MP-1"), "corr-webhook");

        verify(ordenPagoRepository).guardar(any(OrdenPago.class));
        verify(activacionService).activar(any(), any(), any());
        verify(eventPublisher).publicarPagoConfirmado(any(PagoConfirmadoEvent.class));
        verify(webhookLogPort).registrar(eq("MP-1"), anyMap(), eq("PROCESADO"));
        verify(idempotencyPort).liberar("lock:pago_MP-1");
    }

    @Test
    void noRepiteActivacionSiElEstadoYaEraAprobado() {
        lockLibre();
        OrdenPago aprobada = TestFixtures.conIdOrden(TestFixtures.aprobada(100L, "14900"), 10L);
        when(ordenPagoRepository.buscarPorIdPagoExterno("MP-1")).thenReturn(Optional.of(aprobada));
        when(mercadoPago.consultarPago("MP-1")).thenReturn(pago("approved", null, null));

        useCase.procesar(notificacion("MP-1"), "corr");

        verify(ordenPagoRepository, never()).guardar(any());
        verify(activacionService, never()).activar(any(), any(), any());
        verify(eventPublisher, never()).publicarPagoConfirmado(any());
        verify(webhookLogPort).registrar(eq("MP-1"), anyMap(), eq("PROCESADO"));
    }

    @Test
    void actualizaPagoRechazadoSinActivar() {
        lockLibre();
        OrdenPago pendiente = TestFixtures.conIdOrden(TestFixtures.pendiente(100L, "14900"), 10L);
        when(ordenPagoRepository.buscarPorIdPagoExterno("MP-1")).thenReturn(Optional.of(pendiente));
        when(mercadoPago.consultarPago("MP-1")).thenReturn(pago("rejected", null, null));
        when(ordenPagoRepository.guardar(any(OrdenPago.class))).thenAnswer(inv -> inv.getArgument(0));

        useCase.procesar(notificacion("MP-1"), "corr");

        verify(ordenPagoRepository).guardar(any(OrdenPago.class));
        verify(activacionService, never()).activar(any(), any(), any());
        verify(eventPublisher, never()).publicarPagoConfirmado(any());
    }

    @Test
    void ignoraEstadosDesconocidos() {
        lockLibre();
        OrdenPago pendiente = TestFixtures.conIdOrden(TestFixtures.pendiente(100L, "14900"), 10L);
        when(ordenPagoRepository.buscarPorIdPagoExterno("MP-1")).thenReturn(Optional.of(pendiente));
        when(mercadoPago.consultarPago("MP-1")).thenReturn(pago("estado-raro", null, null));

        useCase.procesar(notificacion("MP-1"), "corr");

        verify(ordenPagoRepository, never()).guardar(any());
        verify(idempotencyPort).liberar("lock:pago_MP-1");
    }

    @Test
    void asociaPorReferenciaExternaSUS() {
        lockLibre();
        Suscripcion suscripcion = TestFixtures.conIdSuscripcion(TestFixtures.pendienteUsuario("USER-1"), 100L);
        OrdenPago pendiente = TestFixtures.conIdOrden(TestFixtures.pendiente(100L, "14900"), 10L);
        when(ordenPagoRepository.buscarPorIdPagoExterno("MP-1")).thenReturn(Optional.empty());
        when(mercadoPago.consultarPago("MP-1")).thenReturn(pago("approved", "SUS-100", null));
        when(suscripcionRepository.buscarPorId(100L)).thenReturn(Optional.of(suscripcion));
        when(ordenPagoRepository.buscarPendientePorSuscripcion(100L)).thenReturn(Optional.of(pendiente));
        when(ordenPagoRepository.guardar(any(OrdenPago.class))).thenAnswer(inv -> inv.getArgument(0));
        when(activacionService.activar(any(), any(), any())).thenReturn(suscripcion);

        useCase.procesar(notificacion("MP-1"), "corr");

        verify(activacionService).activar(any(), any(), any());
        verify(eventPublisher).publicarPagoConfirmado(any());
    }

    @Test
    void asociaPorPreapprovalCuandoNoHayReferencia() {
        lockLibre();
        Suscripcion suscripcion = TestFixtures.conIdSuscripcion(TestFixtures.pendienteUsuario("USER-1"), 100L);
        OrdenPago pendiente = TestFixtures.conIdOrden(TestFixtures.pendiente(100L, "14900"), 10L);
        when(ordenPagoRepository.buscarPorIdPagoExterno("MP-1")).thenReturn(Optional.empty());
        when(mercadoPago.consultarPago("MP-1")).thenReturn(pago("approved", null, "PRE-100"));
        when(suscripcionRepository.buscarPorPreapprovalId("PRE-100")).thenReturn(Optional.of(suscripcion));
        when(ordenPagoRepository.buscarPendientePorSuscripcion(100L)).thenReturn(Optional.of(pendiente));
        when(ordenPagoRepository.guardar(any(OrdenPago.class))).thenAnswer(inv -> inv.getArgument(0));
        when(activacionService.activar(any(), any(), any())).thenReturn(suscripcion);
        when(suscripcionRepository.buscarPorId(100L)).thenReturn(Optional.of(suscripcion));

        useCase.procesar(notificacion("MP-1"), "corr");

        verify(activacionService).activar(any(), any(), any());
    }

    @Test
    void creaOrdenDeRenovacionSiNoExistePendiente() {
        lockLibre();
        Suscripcion suscripcion = TestFixtures.conIdSuscripcion(TestFixtures.activaUsuario("USER-1",
                TestFixtures.HOY.minusMonths(1), TestFixtures.HOY), 100L);
        when(ordenPagoRepository.buscarPorIdPagoExterno("MP-1")).thenReturn(Optional.empty());
        when(mercadoPago.consultarPago("MP-1")).thenReturn(pago("approved", "SUS-100", null));
        when(suscripcionRepository.buscarPorId(100L)).thenReturn(Optional.of(suscripcion));
        when(ordenPagoRepository.buscarPendientePorSuscripcion(100L)).thenReturn(Optional.empty());
        when(ordenPagoRepository.guardar(any(OrdenPago.class)))
                .thenAnswer(inv -> TestFixtures.conIdOrden(inv.getArgument(0), 77L));
        when(activacionService.activar(any(), any(), any())).thenReturn(suscripcion);

        useCase.procesar(notificacion("MP-1"), "corr");

        verify(ordenPagoRepository, org.mockito.Mockito.times(2)).guardar(any(OrdenPago.class));
        verify(eventPublisher).publicarPagoConfirmado(any());
    }

    @Test
    void usaElMontoDeLaUltimaOrdenSiLaPasarelaNoLoInforma() {
        lockLibre();
        Suscripcion suscripcion = TestFixtures.conIdSuscripcion(TestFixtures.pendienteUsuario("USER-1"), 100L);
        OrdenPago anterior = TestFixtures.conIdOrden(TestFixtures.aprobada(100L, "29900.00"), 30L);
        when(ordenPagoRepository.buscarPorIdPagoExterno("MP-1")).thenReturn(Optional.empty());
        when(mercadoPago.consultarPago("MP-1")).thenReturn(new MercadoPagoPort.PagoMp(
                "MP-1", "approved", "SUS-100", null, null, null));
        when(suscripcionRepository.buscarPorId(100L)).thenReturn(Optional.of(suscripcion));
        when(ordenPagoRepository.buscarPendientePorSuscripcion(100L)).thenReturn(Optional.empty());
        when(ordenPagoRepository.buscarPorSuscripcion(100L)).thenReturn(List.of(anterior));
        when(ordenPagoRepository.guardar(any(OrdenPago.class)))
                .thenAnswer(inv -> TestFixtures.conIdOrden(inv.getArgument(0), 78L));
        when(activacionService.activar(any(), any(), any())).thenReturn(suscripcion);

        useCase.procesar(notificacion("MP-1"), "corr");

        verify(ordenPagoRepository, org.mockito.Mockito.times(2)).guardar(any(OrdenPago.class));
    }

    @Test
    void fallaSiMercadoPagoNoRespondeYLiberaElLock() {
        lockLibre();
        when(webhookLogPort.existe("MP-1")).thenReturn(false);
        when(mercadoPago.consultarPago("MP-1")).thenThrow(new IllegalStateException("caído"));

        assertThatThrownBy(() -> useCase.procesar(notificacion("MP-1"), "corr"))
                .isInstanceOf(PaymentProcessingException.class);
        verify(idempotencyPort).liberar("lock:pago_MP-1");
    }

    @Test
    void procesaNotificacionSinTipoComoPago() {
        lockLibre();
        when(webhookLogPort.existe("MP-1")).thenReturn(false);
        when(mercadoPago.consultarPago("MP-1")).thenReturn(pago("pending", null, null));
        when(ordenPagoRepository.buscarPorIdPagoExterno("MP-1")).thenReturn(Optional.of(
                TestFixtures.conIdOrden(TestFixtures.pendiente(100L, "14900"), 10L)));

        useCase.procesar(new NotificacionPago("MP-1", null, null, Map.of()), "corr");

        verify(webhookLogPort).registrar(eq("MP-1"), anyMap(), eq("PROCESADO"));
    }
}
