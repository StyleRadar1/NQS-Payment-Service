package edu.dosw.styleradar.pagos.application.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * SR-RF-98: reembolso total o parcial con monto mayor que cero y motivo
 * obligatorio; el acumulado no puede superar el valor original.
 */
public record ReembolsarPagoRequest(
        @NotNull(message = "El monto del reembolso es obligatorio.")
        @DecimalMin(value = "0.01", message = "El monto del reembolso debe ser mayor que cero.")
        BigDecimal monto,

        @NotBlank(message = "Debes indicar el motivo del reembolso.")
        @Size(max = 255, message = "El motivo del reembolso no puede superar 255 caracteres.")
        String motivo) {
}
