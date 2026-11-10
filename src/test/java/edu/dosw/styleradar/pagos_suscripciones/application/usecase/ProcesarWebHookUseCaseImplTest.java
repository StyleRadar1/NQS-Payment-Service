package edu.dosw.styleradar.pagos_suscripciones.application.usecase;

import edu.dosw.styleradar.pagos_suscripciones.domain.exception.IdempotencyException;
import edu.dosw.styleradar.pagos_suscripciones.domain.model.*;
import edu.dosw.styleradar.pagos_suscripciones.domain.port.out.*;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProcesarWebhookUseCaseImplTest {

    @Mock private IdempotencyPort idempotencyPort;
    @Mock private OrdenPagoRepositoryPort ordenPagoRepository;
    @Mock private SuscripcionRepositoryPort suscripcionRepository;
    @Mock private EventPublisherPort eventPublisher;

    @InjectMocks
    private ProcesarWebhookUseCaseImpl useCase;

    private OrdenPago ordenPago;
    private Suscripcion suscripcion;

    @BeforeEach
    void setUp() {
        ordenPago = new OrdenPago(1L, 100L, new Monto(BigDecimal.valueOf(14900), "COP"), "MP-123");
        suscripcion = new Suscripcion(100L, "USER-1", TipoSuscriptor.USUARIO, 2L);
    }

    @Test
    void debeLanzarExcepcionSiNoPuedeAdquirirLock() {
        when(idempotencyPort.adquirirLock("lock:pago_MP-123")).thenReturn(false);

        assertThrows(IdempotencyException.class, () -> 
            useCase.procesarNotificacionPago("MP-123", "APROBADO", "payment")
        );
        verifyNoInteractions(ordenPagoRepository, suscripcionRepository, eventPublisher);
    }

    @Test
    void debeSerIdempotenteSiYaFueProcesado() {
        when(idempotencyPort.adquirirLock(any())).thenReturn(true);
        when(ordenPagoRepository.existePorIdPagoExterno("MP-123")).thenReturn(true);

        useCase.procesarNotificacionPago("MP-123", "APROBADO", "payment");

        verify(ordenPagoRepository, never()).guardar(any());
        verify(idempotencyPort).liberarLock("lock:pago_MP-123");
    }

    @Test
    void debeProcesarPagoAprobadoYPublicarEvento() {
        when(idempotencyPort.adquirirLock(any())).thenReturn(true);
        when(ordenPagoRepository.existePorIdPagoExterno("MP-123")).thenReturn(false);
        when(ordenPagoRepository.buscarPorIdPagoExterno("MP-123")).thenReturn(Optional.of(ordenPago));
        when(suscripcionRepository.buscarPorId(100L)).thenReturn(Optional.of(suscripcion));

        // Cambiado a APROBADO
        useCase.procesarNotificacionPago("MP-123", "APROBADO", "payment");

        verify(ordenPagoRepository, times(1)).guardar(ordenPago);
        verify(suscripcionRepository, times(1)).guardar(suscripcion);
        verify(eventPublisher, times(1)).publicarPagoConfirmado(suscripcion);
        assertEquals(EstadoSuscripcion.ACTIVA, suscripcion.getEstado());
        assertNotNull(suscripcion.getFechaFin());
        verify(idempotencyPort).liberarLock(any());
    }

    @Test
    void noDebeActivarSuscripcionSiPagoEsRechazado() {
        when(idempotencyPort.adquirirLock(any())).thenReturn(true);
        when(ordenPagoRepository.existePorIdPagoExterno("MP-123")).thenReturn(false);
        when(ordenPagoRepository.buscarPorIdPagoExterno("MP-123")).thenReturn(Optional.of(ordenPago));

        // Cambiado a RECHAZADO
        useCase.procesarNotificacionPago("MP-123", "RECHAZADO", "payment");

        verify(ordenPagoRepository, times(1)).guardar(ordenPago);
        verify(suscripcionRepository, never()).guardar(any());
        verify(eventPublisher, never()).publicarPagoConfirmado(any());
        verify(idempotencyPort).liberarLock(any());
    }
}