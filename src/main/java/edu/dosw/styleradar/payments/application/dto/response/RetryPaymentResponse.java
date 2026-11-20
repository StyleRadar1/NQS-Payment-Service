package edu.dosw.styleradar.payments.application.dto.response;

import edu.dosw.styleradar.payments.domain.model.PaymentStatus;

public record RetryPaymentResponse(
        Long orderId,
        PaymentStatus status,
        String initPoint,
        String message) {
}
