package edu.dosw.styleradar.payments.application.dto.response;

import edu.dosw.styleradar.payments.domain.model.PaymentOrder;
import edu.dosw.styleradar.payments.domain.model.PaymentStatus;

import java.time.LocalDateTime;

public record PaymentResponse(
        Long id,
        Long subscriptionId,
        AmountResponse amount,
        PaymentStatus status,
        String externalPaymentId,
        LocalDateTime createdAt) {

    public static PaymentResponse from(PaymentOrder order) {
        return new PaymentResponse(
                order.getId(),
                order.getSubscriptionId(),
                AmountResponse.from(order.getAmount()),
                order.getStatus(),
                order.getExternalPaymentId(),
                order.getCreatedAt());
    }
}
