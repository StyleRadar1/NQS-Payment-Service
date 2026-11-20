package edu.dosw.styleradar.payments.infrastructure.adapter.out.persistence.repository;

import edu.dosw.styleradar.payments.domain.model.SubscriberType;
import edu.dosw.styleradar.payments.infrastructure.adapter.out.persistence.entity.PlanEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface PlanJpaRepository extends JpaRepository<PlanEntity, Long> {

    List<PlanEntity> findByActiveTrueOrderByIdAsc();

    Optional<PlanEntity> findFirstBySubscriberTypeAndActiveTrueAndPriceGreaterThanOrderByIdAsc(
            SubscriberType subscriberType, BigDecimal price);
}
