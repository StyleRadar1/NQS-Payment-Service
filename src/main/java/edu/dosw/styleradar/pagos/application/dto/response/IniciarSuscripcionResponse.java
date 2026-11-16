package edu.dosw.styleradar.pagos.application.dto.response;

import edu.dosw.styleradar.pagos.domain.model.EstadoSuscripcion;

/**
 * SR-RF-67/93/100: enlace de pago, estado inicial PENDIENTE y registro
 * de la orden creada.
 */
public record IniciarSuscripcionResponse(
        Long suscripcionId,
        Long ordenId,
        EstadoSuscripcion estado,
        String initPoint,
        String mensaje) {
}
