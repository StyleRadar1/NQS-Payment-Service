package edu.dosw.styleradar.payments.infrastructure.config;

import edu.dosw.styleradar.payments.infrastructure.adapter.out.persistence.entity.PlanEntity;
import edu.dosw.styleradar.payments.infrastructure.adapter.out.persistence.repository.PlanJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DemoPlanSeederTest {

    @Mock
    private PlanJpaRepository planRepository;

    @Test
    void seedsAlignedTryOnQuotasForRadarAndRadarPlus() {
        when(planRepository.count()).thenReturn(0L);
        DemoPlanSeeder seeder = new DemoPlanSeeder(planRepository);

        seeder.run();

        ArgumentCaptor<PlanEntity> captor = ArgumentCaptor.forClass(PlanEntity.class);
        verify(planRepository, times(3)).save(captor.capture());
        List<PlanEntity> saved = captor.getAllValues();

        PlanEntity radar = saved.stream().filter(p -> p.getId().equals(1L)).findFirst().orElseThrow();
        assertThat(radar.getLimits()).containsEntry("try_on_photo", true);
        assertThat(radar.getLimits()).containsEntry("try_on_uses_per_month", 10);

        PlanEntity radarPlus = saved.stream().filter(p -> p.getId().equals(2L)).findFirst().orElseThrow();
        assertThat(radarPlus.getLimits()).containsEntry("try_on_photo", true);
        assertThat(radarPlus.getLimits()).containsEntry("try_on_uses_per_month", 50);
    }
}
