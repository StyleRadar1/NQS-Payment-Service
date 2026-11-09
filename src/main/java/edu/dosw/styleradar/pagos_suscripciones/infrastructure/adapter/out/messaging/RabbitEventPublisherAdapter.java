package edu.dosw.styleradar.pagos_suscripciones.infrastructure.adapter.out.messaging;

import edu.dosw.styleradar.pagos_suscripciones.domain.model.Suscripcion;
import edu.dosw.styleradar.pagos_suscripciones.domain.port.out.EventPublisherPort;
import edu.dosw.styleradar.pagos_suscripciones.infrastructure.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class RabbitEventPublisherAdapter implements EventPublisherPort {

    private final RabbitTemplate rabbitTemplate;

    @Override
    public void publicarPagoConfirmado(Suscripcion suscripcion) {
        Map<String, Object> event = Map.of(
            "suscripcionId", suscripcion.getId(),
            "suscriptorId", suscripcion.getSuscriptorId(),
            "planId", suscripcion.getPlanId(),
            "fechaFin", suscripcion.getFechaFin().toString()
        );
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.RK_PAGO_CONFIRMADO, event);
    }

    @Override
    public void publicarSuscripcionSuspendida(Suscripcion suscripcion) {
        Map<String, Object> event = Map.of(
            "suscripcionId", suscripcion.getId(),
            "suscriptorId", suscripcion.getSuscriptorId(),
            "motivo", "Falta de pago"
        );
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.RK_SUSCRIPCION_SUSPENDIDA, event);
    }
}