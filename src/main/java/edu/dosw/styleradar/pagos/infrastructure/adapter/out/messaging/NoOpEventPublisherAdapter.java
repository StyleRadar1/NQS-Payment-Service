package edu.dosw.styleradar.pagos.infrastructure.adapter.out.messaging;

import edu.dosw.styleradar.pagos.application.port.out.EventPublisherPort;
import edu.dosw.styleradar.pagos.domain.event.PagoConfirmadoEvent;
import edu.dosw.styleradar.pagos.domain.event.SuscripcionSuspendidaEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Publicador de reemplazo cuando RabbitMQ está deshabilitado (por ejemplo
 * en pruebas unitarias o en un arranque local sin broker). Solo registra el
 * evento para no romper el flujo de negocio.
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "styleradar.rabbit", name = "enabled",
        havingValue = "false")
public class NoOpEventPublisherAdapter implements EventPublisherPort {

    @Override
    public void publicarPagoConfirmado(PagoConfirmadoEvent evento) {
        log.info("[RabbitMQ deshabilitado] pago.confirmado {}", evento.eventId());
    }

    @Override
    public void publicarSuscripcionSuspendida(SuscripcionSuspendidaEvent evento) {
        log.info("[RabbitMQ deshabilitado] suscripcion.suspendida {}", evento.eventId());
    }
}
