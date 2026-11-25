package edu.dosw.styleradar.payments.infrastructure.adapter.out.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.dosw.styleradar.payments.application.port.out.EventPublisherPort;
import edu.dosw.styleradar.payments.domain.event.PaymentConfirmedEvent;
import edu.dosw.styleradar.payments.domain.event.SubscriptionPlanUpdatedEvent;
import edu.dosw.styleradar.payments.domain.event.SubscriptionSuspendedEvent;
import edu.dosw.styleradar.payments.domain.exception.PaymentProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Date;

@Slf4j
@Component

@ConditionalOnProperty(prefix = "styleradar.rabbit", name = "enabled",
        havingValue = "true", matchIfMissing = true)
public class RabbitEventPublisherAdapter implements EventPublisherPort {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;
    private final String exchange;
    private final String paymentConfirmedKey;
    private final String subscriptionSuspendedKey;
    private final String subscriptionPlanUpdatedKey;

    public RabbitEventPublisherAdapter(RabbitTemplate rabbitTemplate,
                                       ObjectMapper objectMapper,
                                       @Value("${styleradar.rabbit.exchange}") String exchange,
                                       @Value("${styleradar.rabbit.payment-confirmed-key}") String paymentConfirmedKey,
                                       @Value("${styleradar.rabbit.subscription-suspended-key}") String subscriptionSuspendedKey,
                                       @Value("${styleradar.rabbit.subscription-plan-updated-key}") String subscriptionPlanUpdatedKey) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
        this.exchange = exchange;
        this.paymentConfirmedKey = paymentConfirmedKey;
        this.subscriptionSuspendedKey = subscriptionSuspendedKey;
        this.subscriptionPlanUpdatedKey = subscriptionPlanUpdatedKey;
    }

    @Override
    public void publishPaymentConfirmed(PaymentConfirmedEvent event) {
        publish(paymentConfirmedKey, event, event.eventId().toString(), event.eventType(),
                event.timestamp());
    }

    @Override
    public void publishSubscriptionSuspended(SubscriptionSuspendedEvent event) {
        publish(subscriptionSuspendedKey, event, event.eventId().toString(), event.eventType(),
                event.timestamp());
    }

    @Override
    public void publishSubscriptionPlanUpdated(SubscriptionPlanUpdatedEvent event) {
        publish(subscriptionPlanUpdatedKey, event, event.eventId().toString(), event.eventType(),
                event.timestamp());
    }

    private void publish(String routingKey, Object event, String eventId, String eventType,
                         java.time.Instant timestamp) {
        String json;
        try {
            json = objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException ex) {
            throw new PaymentProcessingException("No fue posible serializar el evento " + eventType, ex);
        }
        MessagePostProcessor postProcessor = message -> {
            message.getMessageProperties().setContentType("application/json");
            message.getMessageProperties().setContentEncoding(StandardCharsets.UTF_8.name());
            message.getMessageProperties().setMessageId(eventId);
            message.getMessageProperties().setTimestamp(Date.from(timestamp));
            message.getMessageProperties().setHeader("eventType", eventType);
            return message;
        };
        try {
            rabbitTemplate.convertAndSend(exchange, routingKey, json, postProcessor);
            log.info("Event {} published to {}/{}", eventType, exchange, routingKey);
        } catch (AmqpException ex) {
            throw new PaymentProcessingException("No fue posible publicar el evento " + eventType, ex);
        }
    }
}


