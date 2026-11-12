package edu.dosw.styleradar.pagos_suscripciones.application.dto.event;

import edu.dosw.styleradar.pagos_suscripciones.domain.model.TipoSuscriptor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

public record PagoConfirmadoEvent(
        String eventId,
        String eventType,
        int version,
        LocalDateTime occurredAt,
        Long suscripcionId,
        String suscriptorId,
        TipoSuscriptor tipoSuscriptor,
        Long planId,
        String planNombre,
        Map<String, Object> limites,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        BigDecimal monto,
        String moneda
) {
    public static final String TYPE = "pago.confirmado";
    public static final int VERSION = 1;
}