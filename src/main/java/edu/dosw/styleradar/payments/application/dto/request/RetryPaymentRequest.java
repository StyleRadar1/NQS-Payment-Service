package edu.dosw.styleradar.payments.application.dto.request;

import jakarta.validation.constraints.NotBlank;

public record RetryPaymentRequest(
        @NotBlank(message = "Ingresa un correo electrónico válido para realizar el pago.")
        String payerEmail) {
}
