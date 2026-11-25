package edu.dosw.styleradar.payments.application.dto.request;

import edu.dosw.styleradar.payments.domain.model.SubscriberType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record StartSubscriptionRequest(
        @NotNull(message = "El tipo de suscriptor es obligatorio.")
        SubscriberType subscriberType,

        @NotBlank(message = "Ingresa un correo electrónico válido para realizar el pago.")
        String payerEmail,

        Long planId,

        @Pattern(regexp = "^\\d{9}(-\\d)?$", message = "El NIT debe tener 9 dÃ­gitos, con o sin dÃ­gito de verificaciÃ³n.")
        String taxId,

        String storeId) {
}
