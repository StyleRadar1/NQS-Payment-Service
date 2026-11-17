package edu.dosw.styleradar.pagos.application.port.out;

import edu.dosw.styleradar.pagos.domain.event.PagoConfirmadoEvent;
import edu.dosw.styleradar.pagos.domain.event.SuscripcionSuspendidaEvent;

/** Puerto de salida para la publicación de eventos en RabbitMQ (ADR-07). */
public interface EventPublisherPort {

    void publicarPagoConfirmado(PagoConfirmadoEvent evento);

    void publicarSuscripcionSuspendida(SuscripcionSuspendidaEvent evento);
}
