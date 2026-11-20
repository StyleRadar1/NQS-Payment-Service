package edu.dosw.styleradar.payments.infrastructure.adapter.out.persistence.repository;

import edu.dosw.styleradar.payments.infrastructure.adapter.out.persistence.entity.WebhookLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WebhookLogJpaRepository extends JpaRepository<WebhookLogEntity, Long> {

    boolean existsByExternalPaymentId(String externalPaymentId);
}
