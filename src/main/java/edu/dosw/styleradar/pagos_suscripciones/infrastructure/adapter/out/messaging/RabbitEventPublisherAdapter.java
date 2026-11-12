package edu.dosw.styleradar.pagos_suscripciones.infrastructure.adapter.out.messaging;

import edu.dosw.styleradar.pagos_suscripciones.application.dto.event.PagoConfirmadoEvent;
import edu.dosw.styleradar.pagos_suscripciones.application.dto.event.SuscripcionSuspendidaEvent;
import edu.dosw.styleradar.pagos_suscripciones.domain.port.out.EventPublisherPort;
import edu.dosw.styleradar.pagos_suscripciones.infrastructure.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Profile("!test")
public class RabbitEventPublisherAdapter implements EventPublisherPort {

    private final RabbitTemplate rabbitTemplate;

    @Override
    public void publicarPagoConfirmado(PagoConfirmadoEvent event) {
        rabbitTemplate.convertAndSend(
            RabbitMQConfig.EXCHANGE,
            RabbitMQConfig.RK_PAGO_CONFIRMADO,
            event);
    }

    @Override
    public void publicarSuscripcionSuspendida(SuscripcionSuspendidaEvent event) {
        rabbitTemplate.convertAndSend(
            RabbitMQConfig.EXCHANGE,
            RabbitMQConfig.RK_SUSCRIPCION_SUSPENDIDA,
            event);
    }
}