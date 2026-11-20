package edu.dosw.styleradar.payments.infrastructure.adapter.out.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.dosw.styleradar.payments.application.port.out.EventPublisherPort;
import edu.dosw.styleradar.payments.domain.event.PaymentConfirmedEvent;
import edu.dosw.styleradar.payments.domain.event.SubscriptionSuspendedEvent;
import edu.dosw.styleradar.payments.domain.exception.PaymentProcessingException;
import edu.dosw.styleradar.payments.infrastructure.adapter.out.persistence.entity.OutboxEventEntity;
import edu.dosw.styleradar.payments.infrastructure.adapter.out.persistence.repository.OutboxEventJpaRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Transactional outbox: the event is stored in the same transaction as the
 * payment/subscription change. A scheduler publishes it to RabbitMQ and
 * retries failures, so a broker outage does not lose events.
 */
@Slf4j
@Component
@Primary
@ConditionalOnProperty(prefix = "styleradar.rabbit", name = "enabled",
        havingValue = "true", matchIfMissing = true)
public class OutboxEventPublisherAdapter implements EventPublisherPort {

    private final OutboxEventJpaRepository outboxRepository;
    private final ObjectMapper objectMapper;
    private final String paymentConfirmedKey;
    private final String subscriptionSuspendedKey;

    public OutboxEventPublisherAdapter(OutboxEventJpaRepository outboxRepository,
                                       ObjectMapper objectMapper,
                                       @Value("${styleradar.rabbit.payment-confirmed-key}") String paymentConfirmedKey,
                                       @Value("${styleradar.rabbit.subscription-suspended-key}") String subscriptionSuspendedKey) {
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
        this.paymentConfirmedKey = paymentConfirmedKey;
        this.subscriptionSuspendedKey = subscriptionSuspendedKey;
    }

    @Override
    public void publishPaymentConfirmed(PaymentConfirmedEvent event) {
        save(event, paymentConfirmedKey);
    }

    @Override
    public void publishSubscriptionSuspended(SubscriptionSuspendedEvent event) {
        save(event, subscriptionSuspendedKey);
    }

    private void save(Object event, String routingKey) {
        String eventId;
        String eventType;
        String payload;
        try {
            eventId = (String) event.getClass().getMethod("eventId").invoke(event).toString();
            eventType = (String) event.getClass().getMethod("eventType").invoke(event);
            payload = objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException | ReflectiveOperationException ex) {
            throw new PaymentProcessingException("No fue posible preparar el evento para publicación.", ex);
        }
        LocalDateTime now = LocalDateTime.now();
        outboxRepository.save(OutboxEventEntity.builder()
                .eventId(java.util.UUID.fromString(eventId))
                .eventType(eventType)
                .routingKey(routingKey)
                .payload(payload)
                .status("PENDING")
                .attempts(0)
                .createdAt(now)
                .nextAttemptAt(now)
                .build());
        log.debug("Event {} stored in outbox", eventType);
    }
}

