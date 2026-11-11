package edu.dosw.styleradar.pagos_suscripciones.application.usecase;

import edu.dosw.styleradar.pagos_suscripciones.domain.exception.PaymentProcessingException;
import edu.dosw.styleradar.pagos_suscripciones.domain.model.*;
import edu.dosw.styleradar.pagos_suscripciones.domain.port.out.MercadoPagoPort;
import edu.dosw.styleradar.pagos_suscripciones.domain.port.out.OrdenPagoRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReembolsarPagoUseCaseImplTest {

    @Mock private OrdenPagoRepositoryPort ordenPagoRepository;
    @Mock private MercadoPagoPort mercadoPagoPort;
    @InjectMocks private ReembolsarPagoUseCaseImpl useCase;

    @Test
    void debeReembolsarCorrectamente() {
        OrdenPago orden = new OrdenPago(1L, 1L, new Monto(new BigDecimal("100"), "COP"), "MP-1");
        orden.aprobar();
        when(ordenPagoRepository.buscarPorId(1L)).thenReturn(Optional.of(orden));

        useCase.reembolsarPago(1L, "50", "Motivo test");

        verify(mercadoPagoPort).reembolsarPago("MP-1", "50");
        verify(ordenPagoRepository).guardar(orden);
        assertEquals(EstadoPago.REEMBOLSADO, orden.getEstado());
    }

    @Test
    void debeLanzarErrorSiOrdenNoExiste() {
        when(ordenPagoRepository.buscarPorId(1L)).thenReturn(Optional.empty());
        assertThrows(PaymentProcessingException.class, () -> useCase.reembolsarPago(1L, "50", "Motivo"));
    }

    @Test
    void debeConsultarHistorialPagos() {
        when(ordenPagoRepository.buscarPorSuscriptorId("USER-1")).thenReturn(List.of());
        List<OrdenPago> historial = useCase.consultarHistorialPagos("USER-1");
        assertTrue(historial.isEmpty());
        verify(ordenPagoRepository).buscarPorSuscriptorId("USER-1");
    }
}