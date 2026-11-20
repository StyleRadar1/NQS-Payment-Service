package edu.dosw.styleradar.payments.application.dto.response;

import edu.dosw.styleradar.payments.domain.model.SubscriptionStatus;

import java.time.LocalDate;

public record CancellationResponse(
        Long subscriptionId,
        boolean autoRenewal,
        SubscriptionStatus status,
        LocalDate endDate,
        boolean cancellationConfirmed,
        String message) {
}
