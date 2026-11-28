package edu.dosw.styleradar.payments.application.usecase;

import edu.dosw.styleradar.payments.TestFixtures;
import edu.dosw.styleradar.payments.application.dto.response.PlanResponse;
import edu.dosw.styleradar.payments.application.port.out.PlanRepositoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlanUseCaseTest {

    @Mock
    private PlanRepositoryPort planRepository;

    @Test
    void listaPlanesActivesConPriceYLimits() {
        when(planRepository.listActive())
                .thenReturn(List.of(TestFixtures.planRadarPlus(), TestFixtures.planStore()));

        List<PlanResponse> planes = new PlanUseCase(planRepository).list();

        assertThat(planes).hasSize(2);
        assertThat(planes.get(0).name()).isEqualTo("Radar+");
        assertThat(planes.get(0).price().value()).isEqualByComparingTo("14900.00");
        assertThat(planes.get(0).limits()).containsEntry("probador_foto", true);
    }
}
