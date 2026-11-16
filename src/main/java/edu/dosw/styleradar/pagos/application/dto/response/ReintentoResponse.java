package edu.dosw.styleradar.pagos.application.dto.response;

import edu.dosw.styleradar.pagos.domain.model.EstadoPago;

/** SR-RF-96: nueva orden PENDIENTE y nuevo enlace de pago. */
public record ReintentoResponse(
        Long ordenId,
        EstadoPago estado,
        String initPoint,
        String mensaje) {
}
