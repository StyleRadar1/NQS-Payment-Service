package edu.dosw.styleradar.pagos.application.dto.request;

import jakarta.validation.constraints.NotBlank;

/** SR-RF-96: correo del pagador para el nuevo intento de pago. */
public record ReintentarPagoRequest(
        @NotBlank(message = "Ingresa un correo electrónico válido para realizar el pago.")
        String emailPayer) {
}
