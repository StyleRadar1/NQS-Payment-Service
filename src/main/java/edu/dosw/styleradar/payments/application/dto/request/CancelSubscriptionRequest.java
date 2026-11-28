package edu.dosw.styleradar.payments.application.dto.request;

import jakarta.validation.constraints.NotNull;

public record CancelSubscriptionRequest(
        @NotNull(message = "Debes confirmar la cancelación de la renovación.")
        Boolean confirmed) {
}
