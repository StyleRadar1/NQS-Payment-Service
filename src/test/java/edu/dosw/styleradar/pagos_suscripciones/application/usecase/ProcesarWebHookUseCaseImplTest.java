package edu.dosw.styleradar.pagos_suscripciones.application.usecase;

import edu.dosw.styleradar.pagos_suscripciones.domain.exception.IdempotencyException;
import edu.dosw.styleradar.pagos_suscripciones.domain.exception.PaymentProcessingException;
import edu.dosw.styleradar.pagos_suscripciones.domain.model.*;
import edu.dosw.styleradar.pagos_suscripciones.domain.port.out.*;
import edu.dosw.styleradar.pagos_suscripciones.infrastructure.adapter.out.persistence.repository.WebhookLogJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProcesarWebhookUseCaseImplTest {

    @Mock private IdempotencyPort idempotencyPort;
    @Mock private OrdenPagoRepositoryPort ordenPagoRepository;
    @Mock private SuscripcionRepositoryPort suscripcionRepository;
    @Mock private EventPublisherPort eventPublisher;
    @Mock private WebhookLogJpaRepository webhookLogRepository;

    @InjectMocks
    private ProcesarWebhookUseCaseImpl useCase;

    private OrdenPago ordenPago;
    private Suscripcion suscripcion;

    @BeforeEach
    void setUp() {
        ordenPago = new OrdenPago(1L, 100L, new Monto(BigDecimal.valueOf(14900), "COP"), "MP-123");
        suscripcion = new Suscripcion(100L, "USER-1", TipoSuscriptor.USUARIO, 2L);
        suscripcion.setPreapprovalIdMp("MP-123"); // ID de MP
    }

    @Test
    void debeLanzarExcepcionSiNoPuedeAdquirirLock() {
        when(webhookLogRepository.existsByIdPagoExterno("MP-123")).thenReturn(false);
        when(idempotencyPort.adquirirLock("lock:pago_MP-123")).thenReturn(false);

        assertThrows(IdempotencyException.class, () -> 
            useCase.procesarNotificacionPago("MP-123", "APPROVED", "payment")
        );
        verifyNoInteractions(ordenPagoRepository, suscripcionRepository, eventPublisher);
    }

    @Test
    void debeProcesarPagoAprobadoYPublicarEvento() {
        when(webhookLogRepository.existsByIdPagoExterno("MP-123")).thenReturn(false);
        when(idempotencyPort.adquirirLock(any())).thenReturn(true);
        
        // Simulamos que lo encuentra por preapproval_id
        when(suscripcionRepository.buscarPorPreapprovalId("MP-123")).thenReturn(Optional.of(suscripcion));

        useCase.procesarNotificacionPago("MP-123", "APPROVED", "payment");

        verify(suscripcionRepository, times(1)).guardar(suscripcion);
        verify(eventPublisher, times(1)).publicarPagoConfirmado(suscripcion);
        assertEquals(EstadoSuscripcion.ACTIVA, suscripcion.getEstado());
        verify(idempotencyPort).liberarLock(any());
    }

    @Test
    void noDebeActivarSuscripcionSiPagoEsRechazado() {
        // Al no encontrar la suscripción por preapproval_id, buscará en ordenes_pago
        when(webhookLogRepository.existsByIdPagoExterno("MP-123")).thenReturn(false);
        when(idempotencyPort.adquirirLock(any())).thenReturn(true);
        when(suscripcionRepository.buscarPorPreapprovalId("MP-123")).thenReturn(Optional.empty());
        
        // Simulamos que encuentra la orden
        when(ordenPagoRepository.buscarPorIdPagoExterno("MP-123")).thenReturn(Optional.of(ordenPago));

        useCase.procesarNotificacionPago("MP-123", "RECHAZADO", "payment");

        verify(ordenPagoRepository, times(1)).guardar(ordenPago);
        verify(suscripcionRepository, never()).guardar(any());
        verify(eventPublisher, never()).publicarPagoConfirmado(any());
    }
}