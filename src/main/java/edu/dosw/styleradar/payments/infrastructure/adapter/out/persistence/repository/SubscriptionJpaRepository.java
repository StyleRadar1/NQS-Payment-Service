package edu.dosw.styleradar.payments.infrastructure.adapter.out.persistence.repository;

import edu.dosw.styleradar.payments.domain.model.SubscriptionStatus;
import edu.dosw.styleradar.payments.infrastructure.adapter.out.persistence.entity.SubscriptionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface SubscriptionJpaRepository extends JpaRepository<SubscriptionEntity, Long> {

    Optional<SubscriptionEntity> findFirstBySubscriberIdOrderByIdDesc(String subscriberId);

    Optional<SubscriptionEntity> findFirstBySubscriberIdAndStatusInOrderByIdDesc(
            String subscriberId, Collection<SubscriptionStatus> statuses);

    Optional<SubscriptionEntity> findFirstByMercadoPagoPreapprovalId(String mercadoPagoPreapprovalId);

    List<SubscriptionEntity> findByStatusAndEndDateBefore(SubscriptionStatus status, LocalDate date);
}
