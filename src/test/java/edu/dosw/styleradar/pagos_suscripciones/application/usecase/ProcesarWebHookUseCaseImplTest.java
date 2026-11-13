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
        verifyNoInteractions(ordenPagoRepository, mercadoPagoPort, webhookLogRepository, suscripcionActivacionService);
    }

    @Test
    void debeLanzarExcepcionSiPagoDataEsNull() {
        when(idempotencyPort.adquirirLock(anyString())).thenReturn(true);
        when(mercadoPagoPort.consultarPago("MP-123")).thenReturn(null);

        assertThrows(RuntimeException.class, () -> 
            useCase.procesarNotificacionPago("MP-123", "updated", "payment", "{}")
        );
    }

    @Test
    void debeLanzarExcepcionSiPagoAutorizadoDataEsNull() {
        when(idempotencyPort.adquirirLock(anyString())).thenReturn(true);
        when(mercadoPagoPort.consultarPagoAutorizado("AUT-123")).thenReturn(null);

        assertThrows(RuntimeException.class, () -> 
            useCase.procesarNotificacionPago("AUT-123", "updated", "subscription_authorized_payment", "{}")
        );
    }

    @Test
    void noDebeProcesarSiEstadoRealEsNull() {
        when(idempotencyPort.adquirirLock(anyString())).thenReturn(true);
        Map<String, Object> pagoData = Map.of("external_reference", "1"); // Sin status
        when(mercadoPagoPort.consultarPago("MP-123")).thenReturn(pagoData);

        useCase.procesarNotificacionPago("MP-123", "updated", "payment", "{}");
        verify(ordenPagoRepository, never()).buscarPorId(any());
    }

    @Test
    void noDebeProcesarSiExternalRefEsNull() {
        when(idempotencyPort.adquirirLock(anyString())).thenReturn(true);
        Map<String, Object> pagoData = Map.of("status", "approved"); // Sin external_reference
        when(mercadoPagoPort.consultarPago("MP-123")).thenReturn(pagoData);

        useCase.procesarNotificacionPago("MP-123", "updated", "payment", "{}");
        verify(ordenPagoRepository, never()).buscarPorId(any());
    }

    @Test
    void noDebeProcesarSiExternalRefEsInvalido() {
        when(idempotencyPort.adquirirLock(anyString())).thenReturn(true);
        Map<String, Object> pagoData = Map.of("status", "approved", "external_reference", "abc");
        when(mercadoPagoPort.consultarPago("MP-123")).thenReturn(pagoData);

        useCase.procesarNotificacionPago("MP-123", "updated", "payment", "{}");
        verify(ordenPagoRepository, never()).buscarPorId(any());
    }

    @Test
    void noDebeProcesarSiEstadoEsDesconocido() {
        when(idempotencyPort.adquirirLock(anyString())).thenReturn(true);
        Map<String, Object> pagoData = Map.of("status", "weird_status", "external_reference", "1");
        when(mercadoPagoPort.consultarPago("MP-123")).thenReturn(pagoData);

        useCase.procesarNotificacionPago("MP-123", "updated", "payment", "{}");
        verify(ordenPagoRepository, never()).buscarPorId(any());
    }

    @Test
    void debeLanzarExcepcionSiOrdenLocalNoExiste() {
        when(idempotencyPort.adquirirLock(anyString())).thenReturn(true);
        Map<String, Object> pagoData = Map.of("status", "approved", "external_reference", "1");
        when(mercadoPagoPort.consultarPago("MP-123")).thenReturn(pagoData);
        when(ordenPagoRepository.buscarPorId(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> 
            useCase.procesarNotificacionPago("MP-123", "updated", "payment", "{}")
        );
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
        verify(suscripcionActivacionService).activarYExtenderSuscripcion(o);
    }

    @Test
    void debeActivarSiPagoAutorizadoEsApproved() {
        OrdenPago o = orden();
        Map<String, Object> pagoData = Map.of("status", "approved", "external_reference", "1");
        when(mercadoPagoPort.consultarPagoAutorizado("AUT-123")).thenReturn(pagoData);
        when(ordenPagoRepository.buscarPorId(1L)).thenReturn(Optional.of(o));
        when(webhookLogRepository.existsByIdPagoExterno("AUT-123")).thenReturn(false);
        when(idempotencyPort.adquirirLock(anyString())).thenReturn(true);

        useCase.procesarNotificacionPago("AUT-123", "payment.updated", "subscription_authorized_payment", "{}");

        assertEquals(EstadoPago.APROBADO, o.getEstado());
        verify(suscripcionActivacionService).activarYExtenderSuscripcion(o);
    }

    @Test
    void noDebeActivarSiEstadoEsRechazado() {
        OrdenPago o = orden();
        Map<String, Object> pagoData = Map.of("status", "rejected", "external_reference", "1");
        when(mercadoPagoPort.consultarPago("MP-123")).thenReturn(pagoData);
        when(ordenPagoRepository.buscarPorId(1L)).thenReturn(Optional.of(o));
        when(idempotencyPort.adquirirLock(anyString())).thenReturn(true);

        useCase.procesarNotificacionPago("MP-123", "payment.updated", "payment", "{}");

        assertEquals(EstadoPago.RECHAZADO, o.getEstado());
        verify(suscripcionActivacionService, never()).activarYExtenderSuscripcion(any());
    }
    
    @Test
    void debeManejarEventoPreapprovalSinActivar() {
        when(idempotencyPort.adquirirLock(anyString())).thenReturn(true);
        when(webhookLogRepository.existsByIdPagoExterno("PRE-1")).thenReturn(true);

        useCase.procesarNotificacionPago("PRE-1", "updated", "preapproval", "{}");

        verify(suscripcionActivacionService, never()).activarYExtenderSuscripcion(any());
    }
}