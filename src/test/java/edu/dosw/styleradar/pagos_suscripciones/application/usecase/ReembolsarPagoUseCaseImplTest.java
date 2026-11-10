package edu.dosw.styleradar.pagos_suscripciones.application.usecase;

import edu.dosw.styleradar.pagos_suscripciones.domain.model.Monto;
import edu.dosw.styleradar.pagos_suscripciones.domain.model.OrdenPago;
import edu.dosw.styleradar.pagos_suscripciones.domain.port.out.MercadoPagoPort;
import edu.dosw.styleradar.pagos_suscripciones.domain.port.out.OrdenPagoRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReembolsarPagoUseCaseImplTest {

    @Mock private OrdenPagoRepositoryPort repository;
    @Mock private MercadoPagoPort mercadoPagoPort;
    @InjectMocks private ReembolsarPagoUseCaseImpl useCase;

    @Test
    void debeReembolsarCorrectamente() {
        OrdenPago orden = new OrdenPago(1L, 1L, new Monto(BigDecimal.TEN, "COP"), "MP-123");
        orden.aprobar();
        
        when(repository.buscarPorId(1L)).thenReturn(Optional.of(orden));

        useCase.reembolsarPago(1L, "Motivo test");

        verify(mercadoPagoPort).reembolsarPago(eq("MP-123"), anyString());
        verify(repository).guardar(orden);
    }

    @Test
    void debeLanzarErrorSiOrdenNoExiste() {
        when(repository.buscarPorId(1L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> useCase.reembolsarPago(1L, "Motivo"));
    }
}