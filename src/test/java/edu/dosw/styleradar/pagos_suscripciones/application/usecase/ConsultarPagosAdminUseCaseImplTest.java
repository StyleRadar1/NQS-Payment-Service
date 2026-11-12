package edu.dosw.styleradar.pagos_suscripciones.application.usecase;

import edu.dosw.styleradar.pagos_suscripciones.domain.model.*;
import edu.dosw.styleradar.pagos_suscripciones.domain.port.out.OrdenPagoRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConsultarPagosAdminUseCaseImplTest {

    @Mock private OrdenPagoRepositoryPort ordenPagoRepository;
    @InjectMocks private ConsultarPagosAdminUseCaseImpl useCase;

    @Test
    void sinFiltroDebeRetornarTodos() {
        when(ordenPagoRepository.buscarTodos()).thenReturn(List.of(
                new OrdenPago(1L, 100L, new Monto(BigDecimal.TEN, "COP"), "MP-1"),
                new OrdenPago(2L, 100L, new Monto(BigDecimal.ONE, "COP"), "MP-2")
        ));

        List<OrdenPago> resultado = useCase.consultarPagos(null);

        assertEquals(2, resultado.size());
        verify(ordenPagoRepository).buscarTodos();
        verify(ordenPagoRepository, never()).buscarPorEstado(any());
    }

    @Test
    void conFiltroDebeDelegarABuscarPorEstado() {
        when(ordenPagoRepository.buscarPorEstado(EstadoPago.APROBADO)).thenReturn(List.of());

        useCase.consultarPagos(EstadoPago.APROBADO);

        verify(ordenPagoRepository).buscarPorEstado(EstadoPago.APROBADO);
        verify(ordenPagoRepository, never()).buscarTodos();
    }
}