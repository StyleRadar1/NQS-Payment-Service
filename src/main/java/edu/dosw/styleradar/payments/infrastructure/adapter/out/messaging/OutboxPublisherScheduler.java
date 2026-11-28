package edu.dosw.styleradar.payments.infrastructure.adapter.out.messaging;

import edu.dosw.styleradar.payments.infrastructure.adapter.out.persistence.entity.OutboxEventEntity;
import edu.dosw.styleradar.payments.infrastructure.adapter.out.persistence.repository.OutboxEventJpaRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "styleradar.rabbit", name = "enabled",
        havingValue = "true", matchIfMissing = true)
public class OutboxPublisherScheduler {

    private static final int MAX_ATTEMPTS = 10;

    private final OutboxEventJpaRepository outboxRepository;
    private final RabbitTemplate rabbitTemplate;
    private final String exchange;

    public OutboxPublisherScheduler(OutboxEventJpaRepository outboxRepository,
                                    RabbitTemplate rabbitTemplate,
                                    @Value("${styleradar.rabbit.exchange}") String exchange) {
        this.outboxRepository = outboxRepository;
        this.rabbitTemplate = rabbitTemplate;
        this.exchange = exchange;
    }

    @Scheduled(fixedDelayString = "${styleradar.outbox.poll-ms:5000}")
    @Transactional
    public void publishPendingEvents() {
        LocalDateTime now = LocalDateTime.now();
        List<OutboxEventEntity> pending = outboxRepository
                .findTop50ByStatusInAndNextAttemptAtBeforeOrderByCreatedAtAsc(
                        List.of("PENDING", "FAILED"), now);
        for (OutboxEventEntity event : pending) {
            publish(event);
        }
    }

    private void publish(OutboxEventEntity event) {
        try {
            rabbitTemplate.convertAndSend(exchange, event.getRoutingKey(), event.getPayload(),
                    postProcessor(event));
            event.setStatus("PUBLISHED");
            event.setPublishedAt(LocalDateTime.now());
            event.setLastError(null);
        } catch (AmqpException ex) {
            int attempts = event.getAttempts() + 1;
            event.setAttempts(attempts);
            event.setLastError(ex.getMessage() == null ? "RabbitMQ error" : ex.getMessage());
            event.setStatus(attempts >= MAX_ATTEMPTS ? "FAILED" : "PENDING");
            long delaySeconds = Math.min(300, 1L << Math.min(attempts, 8));
            event.setNextAttemptAt(LocalDateTime.now().plusSeconds(delaySeconds));
            log.warn("Could not publish outbox event {}: {}", event.getEventId(), ex.getMessage());
        }
        outboxRepository.save(event);
    }

    private MessagePostProcessor postProcessor(OutboxEventEntity event) {
        return message -> {
            message.getMessageProperties().setContentType("application/json");
            message.getMessageProperties().setContentEncoding(StandardCharsets.UTF_8.name());
            message.getMessageProperties().setMessageId(event.getEventId().toString());
            message.getMessageProperties().setTimestamp(Date.from(java.time.Instant.now()));
            message.getMessageProperties().setHeader("eventType", event.getEventType());
            return message;
        };
    }
}
