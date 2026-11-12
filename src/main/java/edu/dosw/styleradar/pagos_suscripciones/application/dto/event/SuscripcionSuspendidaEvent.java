
package edu.dosw.styleradar.pagos_suscripciones.application.dto.event;

import edu.dosw.styleradar.pagos_suscripciones.domain.model.TipoSuscriptor;
import java.time.LocalDateTime;

public record SuscripcionSuspendidaEvent(
        String eventId,
        String eventType,
        int version,
        LocalDateTime occurredAt,
        Long suscripcionId,
        String suscriptorId,
        TipoSuscriptor tipoSuscriptor,
        Long planAnteriorId,
        Long planNuevoId,
        String motivo
) {
    public static final String TYPE = "suscripcion.suspendida";
    public static final int VERSION = 1;
}