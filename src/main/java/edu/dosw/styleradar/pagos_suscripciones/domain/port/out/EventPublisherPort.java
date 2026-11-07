package edu.dosw.styleradar.pagos_suscripciones.domain.port.out;

import edu.dosw.styleradar.pagos_suscripciones.domain.model.Suscripcion;

public interface EventPublisherPort {
    void publicarPagoConfirmado(Suscripcion suscripcion);
    void publicarSuscripcionSuspendida(Suscripcion suscripcion);
}