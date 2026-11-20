package edu.dosw.styleradar.payments.infrastructure.adapter.out.messaging;

import edu.dosw.styleradar.payments.infrastructure.adapter.out.persistence.entity.OutboxEventEntity;
import edu.dosw.styleradar.payments.infrastructure.adapter.out.persistence.repository.OutboxEventJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OutboxPublisherSchedulerTest {

    @Mock
    private OutboxEventJpaRepository repository;
    @Mock
    private RabbitTemplate rabbitTemplate;

    @Test
    void publishesAndMarksEvent() {
        OutboxEventEntity event = OutboxEventEntity.builder()
                .id(1L)
                .eventId(UUID.randomUUID())
                .eventType("styleradar.pagos.pago.confirmado")
                .routingKey("styleradar.pagos.pago.confirmado")
                .payload("{}")
                .status("PENDING")
                .attempts(0)
                .createdAt(LocalDateTime.now())
                .nextAttemptAt(LocalDateTime.now().minusSeconds(1))
                .build();
        when(repository.findTop50ByStatusInAndNextAttemptAtBeforeOrderByCreatedAtAsc(any(), any()))
                .thenReturn(List.of(event));
        OutboxPublisherScheduler scheduler = new OutboxPublisherScheduler(
                repository, rabbitTemplate, "styleradar.exchange");

        scheduler.publishPendingEvents();

        assertThat(event.getStatus()).isEqualTo("PUBLISHED");
        assertThat(event.getPublishedAt()).isNotNull();
        verify(rabbitTemplate).convertAndSend(anyString(), anyString(), anyString(), any(org.springframework.amqp.core.MessagePostProcessor.class));
        verify(repository).save(event);
    }
}
