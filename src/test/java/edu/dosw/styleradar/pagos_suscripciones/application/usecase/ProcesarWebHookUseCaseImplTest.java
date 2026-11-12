package edu.dosw.styleradar.pagos_suscripciones.application.usecase;

import edu.dosw.styleradar.pagos_suscripciones.domain.exception.IdempotencyException;
import edu.dosw.styleradar.pagos_suscripciones.domain.model.*;
import edu.dosw.styleradar.pagos_suscripciones.domain.port.out.*;
import edu.dosw.styleradar.pagos_suscripciones.application.service.SuscripcionActivacionService;
import edu.dosw.styleradar.pagos_suscripciones.infrastructure.adapter.out.persistence.repository.WebhookLogJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProcesarWebhookUseCaseImplTest {

    @Mock private IdempotencyPort idempotencyPort;
    @Mock private OrdenPagoRepositoryPort ordenPagoRepository;
    @Mock private MercadoPagoPort mercadoPagoPort;
    @Mock private WebhookLogJpaRepository webhookLogRepository;
    @Mock private SuscripcionActivacionService suscripcionActivacionService;

    @InjectMocks private ProcesarWebhookUseCaseImpl useCase;

    private OrdenPago orden() {
        return new OrdenPago(1L, 100L, new Monto(new BigDecimal("14900"), "COP"), "MP-123");
    }

    private Suscripcion subPendiente() {
        return new Suscripcion(100L, "U", TipoSuscriptor.USUARIO, 2L);
    }

    @Test
    void debeLanzarSiNoAdquiereLock() {
        when(idempotencyPort.adquirirLock(anyString())).thenReturn(false);

        assertThrows(IdempotencyException.class,
                () -> useCase.procesarNotificacionPago("MP-123", "updated", "payment", "{}"));

        verifyNoInteractions(ordenPagoRepository, mercadoPagoPort,
                webhookLogRepository, suscripcionActivacionService);
    }

    @Test
    void debeActivarSiEstadoRealEsApproved() {
        OrdenPago o = orden();
        Suscripcion s = subPendiente();
        when(webhookLogRepository.existsByIdPagoExterno("MP-123")).thenReturn(false);
        when(idempotencyPort.adquirirLock(anyString())).thenReturn(true);
        when(mercadoPagoPort.consultarEstadoPago("MP-123")).thenReturn("approved");
        when(ordenPagoRepository.buscarPorIdPagoExterno("MP-123")).thenReturn(Optional.of(o));

        useCase.procesarNotificacionPago("MP-123", "payment.updated", "payment", "{}");

        assertEquals(EstadoPago.APROBADO, o.getEstado());
        verify(suscripcionActivacionService).activarYExtenderSuscripcion(o);
    }

    @Test
    void noDebeActivarSiEstadoRealEsRejected() {
        OrdenPago o = orden();
        when(webhookLogRepository.existsByIdPagoExterno("MP-123")).thenReturn(false);
        when(idempotencyPort.adquirirLock(anyString())).thenReturn(true);
        when(mercadoPagoPort.consultarEstadoPago("MP-123")).thenReturn("rejected");
        when(ordenPagoRepository.buscarPorIdPagoExterno("MP-123")).thenReturn(Optional.of(o));

        useCase.procesarNotificacionPago("MP-123", "payment.updated", "payment", "{}");

        assertEquals(EstadoPago.RECHAZADO, o.getEstado());
        verify(suscripcionActivacionService, never()).activarYExtenderSuscripcion(any());
    }

    @Test
    void noDebeActivarSiEstadoRealEsPending() {
        OrdenPago o = orden();
        when(webhookLogRepository.existsByIdPagoExterno("MP-123")).thenReturn(false);
        when(idempotencyPort.adquirirLock(anyString())).thenReturn(true);
        when(mercadoPagoPort.consultarEstadoPago("MP-123")).thenReturn("pending");
        when(ordenPagoRepository.buscarPorIdPagoExterno("MP-123")).thenReturn(Optional.of(o));

        useCase.procesarNotificacionPago("MP-123", "payment.updated", "payment", "{}");

        assertEquals(EstadoPago.PENDIENTE, o.getEstado());
        verify(suscripcionActivacionService, never()).activarYExtenderSuscripcion(any());
    }

    @Test
    void renovacionNoDebePerderDiasPagados() {
        // Este test verifica el comportamiento de Suscripcion.renovar(),
        // que ahora se prueba de forma unitaria en SuscripcionTest.
        // Aquí validamos que se delega a SuscripcionActivacionService.
        OrdenPago o = orden();
        when(webhookLogRepository.existsByIdPagoExterno("MP-123")).thenReturn(false);
        when(idempotencyPort.adquirirLock(anyString())).thenReturn(true);
        when(mercadoPagoPort.consultarEstadoPago("MP-123")).thenReturn("approved");
        when(ordenPagoRepository.buscarPorIdPagoExterno("MP-123")).thenReturn(Optional.of(o));

        useCase.procesarNotificacionPago("MP-123", "payment.updated", "payment", "{}");

        verify(suscripcionActivacionService).activarYExtenderSuscripcion(o);
    }

    @Test
    void noDebeReprocesarSiOrdenYaEstaEnMismoEstado() {
        // Fix P0: idempotencia. Si la orden ya está APROBADA y MP sigue "approved", skip.
        OrdenPago o = orden();
        o.aprobar(); // estado ya APROBADO
        when(webhookLogRepository.existsByIdPagoExterno("MP-123")).thenReturn(true);
        when(idempotencyPort.adquirirLock(anyString())).thenReturn(true);
        when(mercadoPagoPort.consultarEstadoPago("MP-123")).thenReturn("approved");
        when(ordenPagoRepository.buscarPorIdPagoExterno("MP-123")).thenReturn(Optional.of(o));

        useCase.procesarNotificacionPago("MP-123", "payment.updated", "payment", "{}");

        verify(suscripcionActivacionService, never()).activarYExtenderSuscripcion(any());
        verify(ordenPagoRepository, never()).guardar(any());
    }

    @Test
    void renovarDebeExtenderDesdeFechaFinExistenteSinPerderDias() {
        Suscripcion s = new Suscripcion(1L, "U", TipoSuscriptor.USUARIO, 1L);
        LocalDate finFuturo = LocalDate.now().plusDays(10);
        s.rehidratar(EstadoSuscripcion.ACTIVA, LocalDate.now(), finFuturo, true, null, null);

        s.renovar();

        assertEquals(finFuturo.plusMonths(1), s.getFechaFin());
        assertEquals(EstadoSuscripcion.ACTIVA, s.getEstado());
    }
    @Test
void debeIgnorarSiTipoEventoEsNull() {
    when(idempotencyPort.adquirirLock(anyString())).thenReturn(true);
    when(webhookLogRepository.existsByIdPagoExterno("MP-123")).thenReturn(true);

    useCase.procesarNotificacionPago("MP-123", "updated", null, "{}");

    verifyNoInteractions(mercadoPagoPort);
}

@Test
void debeIgnorarSiIdExternoEsNull() {
    useCase.procesarNotificacionPago(null, "updated", "payment", "{}");
    verifyNoInteractions(idempotencyPort, mercadoPagoPort);
}

@Test
void debeManejarEventoPreapprovalSinActivar() {
    when(idempotencyPort.adquirirLock(anyString())).thenReturn(true);
    when(webhookLogRepository.existsByIdPagoExterno("PRE-1")).thenReturn(true);

    useCase.procesarNotificacionPago("PRE-1", "updated", "preapproval", "{}");

    verify(suscripcionActivacionService, never()).activarYExtenderSuscripcion(any());
}

@Test
void debeIgnorarEstadoDesconocido() {
    when(idempotencyPort.adquirirLock(anyString())).thenReturn(true);
    when(webhookLogRepository.existsByIdPagoExterno("MP-123")).thenReturn(true);
    when(mercadoPagoPort.consultarEstadoPago("MP-123")).thenReturn("weird");

    useCase.procesarNotificacionPago("MP-123", "updated", "payment", "{}");

    verify(suscripcionActivacionService, never()).activarYExtenderSuscripcion(any());
    verify(ordenPagoRepository, never()).guardar(any());
    verify(ordenPagoRepository, never()).buscarPorIdPagoExterno(any());
}
}