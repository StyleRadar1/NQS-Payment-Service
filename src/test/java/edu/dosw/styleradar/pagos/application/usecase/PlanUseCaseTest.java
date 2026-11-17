package edu.dosw.styleradar.pagos.application.usecase;

import edu.dosw.styleradar.pagos.TestFixtures;
import edu.dosw.styleradar.pagos.application.dto.response.PlanResponse;
import edu.dosw.styleradar.pagos.application.port.out.PlanRepositoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PlanUseCase (SR-RF-100/102)")
class PlanUseCaseTest {

    @Mock
    private PlanRepositoryPort planRepository;

    @Test
    void listaPlanesActivosConPrecioYLimites() {
        when(planRepository.listarActivos())
                .thenReturn(List.of(TestFixtures.planRadarPlus(), TestFixtures.planAlmacen()));

        List<PlanResponse> planes = new PlanUseCase(planRepository).listar();

        assertThat(planes).hasSize(2);
        assertThat(planes.get(0).nombre()).isEqualTo("Radar+");
        assertThat(planes.get(0).precio().valor()).isEqualByComparingTo("14900.00");
        assertThat(planes.get(0).limites()).containsEntry("probador_foto", true);
    }
}
