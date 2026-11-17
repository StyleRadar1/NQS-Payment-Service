package edu.dosw.styleradar.pagos.application.dto.request;

import jakarta.validation.constraints.NotNull;

/**
 * SR-RF-68 y SR-RF-101: la cancelación exige confirmación explícita
 * del actor antes de desactivar la renovación automática.
 */
public record CancelarSuscripcionRequest(
        @NotNull(message = "Debes confirmar la cancelación de la renovación.")
        Boolean confirmacion) {
}
