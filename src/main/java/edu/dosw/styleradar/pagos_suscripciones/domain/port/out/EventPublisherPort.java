package edu.dosw.styleradar.pagos_suscripciones.domain.port.out;

import edu.dosw.styleradar.pagos_suscripciones.application.dto.event.PagoConfirmadoEvent;
import edu.dosw.styleradar.pagos_suscripciones.application.dto.event.SuscripcionSuspendidaEvent;

public interface EventPublisherPort {
    void publicarPagoConfirmado(PagoConfirmadoEvent event);
    void publicarSuscripcionSuspendida(SuscripcionSuspendidaEvent event);
}