package edu.dosw.styleradar.payments.application.dto.response;

import edu.dosw.styleradar.payments.domain.model.SubscriptionStatus;

public record StartSubscriptionResponse(
        Long subscriptionId,
        Long orderId,
        SubscriptionStatus status,
        String initPoint,
        String message) {
}
