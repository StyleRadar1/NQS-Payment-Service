package edu.dosw.styleradar.payments.infrastructure.adapter.out.persistence;

import edu.dosw.styleradar.payments.domain.model.Amount;
import edu.dosw.styleradar.payments.domain.model.PaymentOrder;
import edu.dosw.styleradar.payments.domain.model.Plan;
import edu.dosw.styleradar.payments.domain.model.Subscription;
import edu.dosw.styleradar.payments.infrastructure.adapter.out.persistence.entity.PaymentOrderEntity;
import edu.dosw.styleradar.payments.infrastructure.adapter.out.persistence.entity.PlanEntity;
import edu.dosw.styleradar.payments.infrastructure.adapter.out.persistence.entity.SubscriptionEntity;

public final class PersistenceMapper {

    private PersistenceMapper() {
    }

    public static Plan toDomain(PlanEntity entity) {
        return new Plan(
                entity.getId(),
                entity.getName(),
                entity.getSubscriberType(),
                new Amount(entity.getPrice(), entity.getCurrency()),
                entity.getLimits(),
                entity.isActive());
    }

    public static Subscription toDomain(SubscriptionEntity entity) {
        return Subscription.rehydrate(
                entity.getId(),
                entity.getSubscriberId(),
                entity.getSubscriberType(),
                entity.getPlanId(),
                entity.getScheduledPlanId(),
                entity.getStatus(),
                entity.getStartDate(),
                entity.getEndDate(),
                entity.isAutoRenewal(),
                entity.getMercadoPagoPreapprovalId(),
                entity.getTaxId(),
                entity.getStoreId(),
                entity.getUpdatedAt());
    }

    public static SubscriptionEntity toEntity(Subscription subscription) {
        return SubscriptionEntity.builder()
                .id(subscription.getId())
                .planId(subscription.getPlanId())
                .scheduledPlanId(subscription.getScheduledPlanId())
                .subscriberId(subscription.getSubscriberId())
                .subscriberType(subscription.getSubscriberType())
                .status(subscription.getStatus())
                .startDate(subscription.getStartDate())
                .endDate(subscription.getEndDate())
                .autoRenewal(subscription.isAutoRenewal())
                .mercadoPagoPreapprovalId(subscription.getMercadoPagoPreapprovalId())
                .taxId(subscription.getTaxId())
                .storeId(subscription.getStoreId())
                .updatedAt(subscription.getUpdatedAt())
                .build();
    }

    public static PaymentOrder toDomain(PaymentOrderEntity entity) {
        return PaymentOrder.rehydrate(
                entity.getId(),
                entity.getSubscriptionId(),
                new Amount(entity.getAmount(), entity.getCurrency()),
                entity.getStatus(),
                entity.getExternalPaymentId(),
                entity.getCreatedAt());
    }

    public static PaymentOrderEntity toEntity(PaymentOrder order) {
        return PaymentOrderEntity.builder()
                .id(order.getId())
                .subscriptionId(order.getSubscriptionId())
                .amount(order.getAmount().value())
                .currency(order.getAmount().currency())
                .status(order.getStatus())
                .externalPaymentId(order.getExternalPaymentId())
                .createdAt(order.getCreatedAt())
                .build();
    }
}
