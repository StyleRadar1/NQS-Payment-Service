package edu.dosw.styleradar.payments.application.dto.response;

import edu.dosw.styleradar.payments.domain.model.PaymentOrder;
import edu.dosw.styleradar.payments.domain.model.PaymentStatus;
import edu.dosw.styleradar.payments.domain.model.Plan;
import edu.dosw.styleradar.payments.domain.model.SubscriberType;
import edu.dosw.styleradar.payments.domain.model.Subscription;

import java.time.LocalDateTime;

public record PaymentResponse(
        Long id,
        Long subscriptionId,
        String subscriberId,
        SubscriberType subscriberType,
        Long planId,
        String planName,
        AmountResponse amount,
        PaymentStatus status,
        String externalPaymentId,
        LocalDateTime createdAt) {

    /**
     * Compatibilidad para consumidores internos que no necesitan los datos del plan.
     * Los campos nuevos quedan en null.
     */
    public PaymentResponse(Long id,
                           Long subscriptionId,
                           AmountResponse amount,
                           PaymentStatus status,
                           String externalPaymentId,
                           LocalDateTime createdAt) {
        this(id, subscriptionId, null, null, null, null, amount, status, externalPaymentId, createdAt);
    }

    public static PaymentResponse from(PaymentOrder order) {
        return from(order, null, null);
    }

    public static PaymentResponse from(PaymentOrder order, Subscription subscription, Plan plan) {
        return new PaymentResponse(
                order.getId(),
                order.getSubscriptionId(),
                subscription == null ? null : subscription.getSubscriberId(),
                subscription == null ? null : subscription.getSubscriberType(),
                subscription == null ? null : subscription.getPlanId(),
                plan == null ? null : plan.getName(),
                AmountResponse.from(order.getAmount()),
                order.getStatus(),
                order.getExternalPaymentId(),
                order.getCreatedAt());
    }
}
