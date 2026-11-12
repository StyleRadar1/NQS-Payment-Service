package edu.dosw.styleradar.pagos_suscripciones.infrastructure.adapter.out.persistence.repository;

import edu.dosw.styleradar.pagos_suscripciones.infrastructure.adapter.out.persistence.entity.WebhookLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WebhookLogJpaRepository extends JpaRepository<WebhookLogEntity, Long> {
    boolean existsByIdPagoExterno(String idPagoExterno);
}