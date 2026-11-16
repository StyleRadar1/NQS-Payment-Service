package edu.dosw.styleradar.pagos.application.dto.response;

import edu.dosw.styleradar.pagos.domain.model.EstadoSuscripcion;

import java.time.LocalDate;

/**
 * SR-RF-68/101: resultado de la cancelación de la renovación.
 * La suscripción conserva el acceso hasta {@code fechaFin}.
 */
public record CancelacionResponse(
        Long suscripcionId,
        boolean autoRenovacion,
        EstadoSuscripcion estado,
        LocalDate fechaFin,
        boolean cancelacionConfirmada,
        String mensaje) {
}
