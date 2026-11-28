package edu.dosw.styleradar.payments.infrastructure.adapter.out.messaging;

import edu.dosw.styleradar.payments.application.port.out.EventPublisherPort;
import edu.dosw.styleradar.payments.domain.event.PaymentConfirmedEvent;
import edu.dosw.styleradar.payments.domain.event.SubscriptionPlanUpdatedEvent;
import edu.dosw.styleradar.payments.domain.event.SubscriptionSuspendedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "styleradar.rabbit", name = "enabled",
        havingValue = "false")
public class NoOpEventPublisherAdapter implements EventPublisherPort {

    @Override
    public void publishPaymentConfirmed(PaymentConfirmedEvent event) {
        log.info("[RabbitMQ disabled] payment.confirmed {}", event.eventId());
    }

    @Override
    public void publishSubscriptionSuspended(SubscriptionSuspendedEvent event) {
        log.info("[RabbitMQ disabled] subscription.suspended {}", event.eventId());
    }

    @Override
    public void publishSubscriptionPlanUpdated(SubscriptionPlanUpdatedEvent event) {
        log.info("[RabbitMQ disabled] subscription.plan.updated {}", event.eventId());
    }
}
