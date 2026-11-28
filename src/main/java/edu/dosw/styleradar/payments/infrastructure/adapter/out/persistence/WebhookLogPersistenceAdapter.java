package edu.dosw.styleradar.payments.infrastructure.adapter.out.persistence;

import edu.dosw.styleradar.payments.application.port.out.WebhookLogPort;
import edu.dosw.styleradar.payments.infrastructure.adapter.out.persistence.entity.WebhookLogEntity;
import edu.dosw.styleradar.payments.infrastructure.adapter.out.persistence.repository.WebhookLogJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebhookLogPersistenceAdapter implements WebhookLogPort {

    private final WebhookLogJpaRepository webhookLogJpaRepository;

    @Override
    public boolean exists(String externalPaymentId) {
        return webhookLogJpaRepository.existsByExternalPaymentId(externalPaymentId);
    }

    @Override
    public void register(String externalPaymentId, Map<String, Object> payload, String processingStatus) {
        WebhookLogEntity entity = WebhookLogEntity.builder()
                .externalPaymentId(externalPaymentId)
                .receivedPayload(payload)
                .processingStatus(processingStatus)
                .receivedAt(LocalDateTime.now())
                .build();
        try {
            webhookLogJpaRepository.save(entity);
        } catch (DataIntegrityViolationException ex) {
            log.debug("Webhook {} already registered by another transaction", externalPaymentId);
        }
    }
}
