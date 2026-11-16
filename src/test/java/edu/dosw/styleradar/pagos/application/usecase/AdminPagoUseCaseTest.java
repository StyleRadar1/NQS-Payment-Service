package edu.dosw.styleradar.pagos.application.usecase;

import edu.dosw.styleradar.pagos.TestFixtures;
import edu.dosw.styleradar.pagos.application.dto.response.PagoResponse;
import edu.dosw.styleradar.pagos.application.port.out.OrdenPagoRepositoryPort;
import edu.dosw.styleradar.pagos.domain.model.EstadoPago;
import edu.dosw.styleradar.pagos.domain.model.OrdenPago;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminPagoUseCase (SR-RF-75)")
class AdminPagoUseCaseTest {

    @Mock
    private OrdenPagoRepositoryPort ordenPagoRepository;

    @Test
    void listaTodosLosPagos() {
        OrdenPago orden = TestFixtures.conIdOrden(TestFixtures.aprobada(100L, "14900"), 1L);
        when(ordenPagoRepository.listar(null)).thenReturn(List.of(orden));

        List<PagoResponse> pagos = new AdminPagoUseCase(ordenPagoRepository).listar(null);

        assertThat(pagos).hasSize(1);
        assertThat(pagos.get(0).estado()).isEqualTo(EstadoPago.APROBADO);
        verify(ordenPagoRepository).listar(null);
    }

    @Test
    void filtraPorEstado() {
        when(ordenPagoRepository.listar(EstadoPago.REEMBOLSADO)).thenReturn(List.of());

        assertThat(new AdminPagoUseCase(ordenPagoRepository).listar(EstadoPago.REEMBOLSADO)).isEmpty();
    }
}
