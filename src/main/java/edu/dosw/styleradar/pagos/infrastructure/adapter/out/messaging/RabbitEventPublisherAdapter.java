package edu.dosw.styleradar.pagos.infrastructure.adapter.out.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.dosw.styleradar.pagos.application.port.out.EventPublisherPort;
import edu.dosw.styleradar.pagos.domain.event.PagoConfirmadoEvent;
import edu.dosw.styleradar.pagos.domain.event.SuscripcionSuspendidaEvent;
import edu.dosw.styleradar.pagos.domain.exception.PaymentProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Productor de eventos en {@code styleradar.exchange}.
 *
 * <p>ADR-07 y convención del broker: routing keys
 * {@code styleradar.pagos.pago.confirmado} y
 * {@code styleradar.pagos.suscripcion.suspendida}, JSON en UTF-8,
 * fechas ISO-8601 y {@code eventId} único. No se depende del header TypeId.</p>
 */
@Slf4j
@Component
@Primary
@ConditionalOnProperty(prefix = "styleradar.rabbit", name = "enabled",
        havingValue = "true", matchIfMissing = true)
public class RabbitEventPublisherAdapter implements EventPublisherPort {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;
    private final String exchange;
    private final String pagoConfirmadoKey;
    private final String suscripcionSuspendidaKey;

    public RabbitEventPublisherAdapter(RabbitTemplate rabbitTemplate,
                                       ObjectMapper objectMapper,
                                       @Value("${styleradar.rabbit.exchange}") String exchange,
                                       @Value("${styleradar.rabbit.pago-confirmado-key}") String pagoConfirmadoKey,
                                       @Value("${styleradar.rabbit.suscripcion-suspendida-key}") String suscripcionSuspendidaKey) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
        this.exchange = exchange;
        this.pagoConfirmadoKey = pagoConfirmadoKey;
        this.suscripcionSuspendidaKey = suscripcionSuspendidaKey;
    }

    @Override
    public void publicarPagoConfirmado(PagoConfirmadoEvent evento) {
        publicar(pagoConfirmadoKey, evento, evento.eventId().toString(), evento.eventType(),
                evento.timestamp());
    }

    @Override
    public void publicarSuscripcionSuspendida(SuscripcionSuspendidaEvent evento) {
        publicar(suscripcionSuspendidaKey, evento, evento.eventId().toString(), evento.eventType(),
                evento.timestamp());
    }

    private void publicar(String routingKey, Object evento, String eventId, String eventType,
                          java.time.Instant timestamp) {
        String json;
        try {
            json = objectMapper.writeValueAsString(evento);
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
            log.info("Evento {} publicado en {}/{}", eventType, exchange, routingKey);
        } catch (AmqpException ex) {
            throw new PaymentProcessingException("No fue posible publicar el evento " + eventType, ex);
        }
    }
}
