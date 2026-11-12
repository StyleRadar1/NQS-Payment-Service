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
import java.util.Map;
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
        return new OrdenPago(1L, 100L, new Monto(new BigDecimal("14900"), "COP"), null);
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
        Map<String, Object> pagoData = Map.of("status", "approved", "external_reference", "1");
        when(mercadoPagoPort.consultarPago("MP-123")).thenReturn(pagoData);
        when(ordenPagoRepository.buscarPorId(1L)).thenReturn(Optional.of(o));
        when(webhookLogRepository.existsByIdPagoExterno("MP-123")).thenReturn(false);
        when(idempotencyPort.adquirirLock(anyString())).thenReturn(true);

        useCase.procesarNotificacionPago("MP-123", "payment.updated", "payment", "{}");

        assertEquals(EstadoPago.APROBADO, o.getEstado());
        assertEquals("MP-123", o.getIdPagoExterno());
        verify(suscripcionActivacionService).activarYExtenderSuscripcion(o);
    }

    @Test
    void noDebeActivarSiEstadoRealEsRejected() {
        OrdenPago o = orden();
        Map<String, Object> pagoData = Map.of("status", "rejected", "external_reference", "1");
        when(mercadoPagoPort.consultarPago("MP-123")).thenReturn(pagoData);
        when(ordenPagoRepository.buscarPorId(1L)).thenReturn(Optional.of(o));
        when(webhookLogRepository.existsByIdPagoExterno("MP-123")).thenReturn(false);
        when(idempotencyPort.adquirirLock(anyString())).thenReturn(true);

        useCase.procesarNotificacionPago("MP-123", "payment.updated", "payment", "{}");

        assertEquals(EstadoPago.RECHAZADO, o.getEstado());
        verify(suscripcionActivacionService, never()).activarYExtenderSuscripcion(any());
    }

    @Test
    void noDebeReprocesarSiOrdenYaEstaEnMismoEstado() {
        OrdenPago o = orden();
        o.aprobar();
        o.actualizarIdPagoExterno("MP-123");
        
        Map<String, Object> pagoData = Map.of("status", "approved", "external_reference", "1");
        when(mercadoPagoPort.consultarPago("MP-123")).thenReturn(pagoData);
        when(ordenPagoRepository.buscarPorId(1L)).thenReturn(Optional.of(o));
        when(webhookLogRepository.existsByIdPagoExterno("MP-123")).thenReturn(true);
        when(idempotencyPort.adquirirLock(anyString())).thenReturn(true);

        useCase.procesarNotificacionPago("MP-123", "payment.updated", "payment", "{}");

        verify(suscripcionActivacionService, never()).activarYExtenderSuscripcion(any());
        verify(ordenPagoRepository, never()).guardar(any());
    }
    
    @Test
    void debeManejarEventoPreapprovalSinActivar() {
        when(idempotencyPort.adquirirLock(anyString())).thenReturn(true);
        when(webhookLogRepository.existsByIdPagoExterno("PRE-1")).thenReturn(true);

        useCase.procesarNotificacionPago("PRE-1", "updated", "preapproval", "{}");

        verify(suscripcionActivacionService, never()).activarYExtenderSuscripcion(any());
    }
}