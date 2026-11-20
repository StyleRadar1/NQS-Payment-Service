package edu.dosw.styleradar.payments.application.dto.response;

import edu.dosw.styleradar.payments.domain.model.SubscriptionStatus;
import edu.dosw.styleradar.payments.domain.model.Subscription;
import edu.dosw.styleradar.payments.domain.model.SubscriberType;

import java.time.LocalDate;

public record SubscriptionResponse(
        Long id,
        String subscriberId,
        SubscriberType subscriberType,
        PlanResponse plan,
        SubscriptionStatus status,
        LocalDate startDate,
        LocalDate endDate,
        boolean autoRenewal,
        boolean access,
        String taxId,
        java.util.UUID storeId,
        String message) {

    public static SubscriptionResponse from(Subscription subscription,
                                            PlanResponse plan,
                                            boolean access,
                                            String message) {
        return new SubscriptionResponse(
                subscription.getId(),
                subscription.getSubscriberId(),
                subscription.getSubscriberType(),
                plan,
                subscription.getStatus(),
                subscription.getStartDate(),
                subscription.getEndDate(),
                subscription.isAutoRenewal(),
                access,
                subscription.getTaxId(),
                subscription.getStoreId(),
                message);
    }
}
