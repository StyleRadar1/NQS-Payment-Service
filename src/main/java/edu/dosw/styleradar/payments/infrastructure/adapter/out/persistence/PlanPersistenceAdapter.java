package edu.dosw.styleradar.payments.infrastructure.adapter.out.persistence;

import edu.dosw.styleradar.payments.application.port.out.PlanRepositoryPort;
import edu.dosw.styleradar.payments.domain.model.Plan;
import edu.dosw.styleradar.payments.domain.model.SubscriberType;
import edu.dosw.styleradar.payments.infrastructure.adapter.out.persistence.repository.PlanJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PlanPersistenceAdapter implements PlanRepositoryPort {

    private final PlanJpaRepository planJpaRepository;

    @Override
    public Optional<Plan> findById(Long id) {
        return planJpaRepository.findById(id).map(PersistenceMapper::toDomain);
    }

    @Override
    public List<Plan> listActive() {
        return planJpaRepository.findByActiveTrueOrderByIdAsc().stream()
                .map(PersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Plan> findPaidPlanByType(SubscriberType subscriberType) {
        return planJpaRepository
                .findFirstBySubscriberTypeAndActiveTrueAndPriceGreaterThanOrderByIdAsc(
                        subscriberType, BigDecimal.ZERO)
                .map(PersistenceMapper::toDomain);
    }
}
