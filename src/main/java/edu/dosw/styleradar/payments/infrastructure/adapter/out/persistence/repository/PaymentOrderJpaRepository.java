package edu.dosw.styleradar.payments.infrastructure.adapter.out.persistence.repository;

import edu.dosw.styleradar.payments.domain.model.PaymentStatus;
import edu.dosw.styleradar.payments.infrastructure.adapter.out.persistence.entity.PaymentOrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PaymentOrderJpaRepository extends JpaRepository<PaymentOrderEntity, Long> {

    Optional<PaymentOrderEntity> findByExternalPaymentId(String externalPaymentId);

    Optional<PaymentOrderEntity> findFirstBySubscriptionIdAndStatusOrderByCreatedAtDesc(
            Long subscriptionId, PaymentStatus status);

    List<PaymentOrderEntity> findBySubscriptionIdOrderByCreatedAtDesc(Long subscriptionId);

    List<PaymentOrderEntity> findBySubscription_SubscriberIdOrderByCreatedAtDesc(String subscriberId);

    List<PaymentOrderEntity> findAllByOrderByCreatedAtDesc();

    List<PaymentOrderEntity> findByStatusOrderByCreatedAtDesc(PaymentStatus status);

    List<PaymentOrderEntity> findByStatusAndCreatedAtBefore(PaymentStatus status, LocalDateTime limit);
}
