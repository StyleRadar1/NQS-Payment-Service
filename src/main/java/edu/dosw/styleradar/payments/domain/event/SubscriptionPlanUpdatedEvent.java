package edu.dosw.styleradar.payments.domain.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import edu.dosw.styleradar.payments.domain.model.SubscriberType;
import edu.dosw.styleradar.payments.domain.model.Subscription;
import edu.dosw.styleradar.payments.domain.model.SubscriptionStatus;

import java.time.Instant;
import java.util.UUID;

public record SubscriptionPlanUpdatedEvent(
        UUID eventId,
        String eventType,
        int version,
        Instant timestamp,
        String producer,
        String correlationId,
        Data data) {

    public static final String TYPE = "styleradar.pagos.suscripcion.plan.actualizado";
    public static final String PRODUCER_NAME = "ms-pagos-suscripciones";
    public static final int SCHEMA_VERSION = 1;
    public static final String SUSPENDED = "SUSPENDED";

    public record Data(
            @JsonProperty("tipoSuscriptor") String subscriberType,
            @JsonProperty("planAnterior") String previousPlan,
            @JsonProperty("planActual") String currentPlan) {
    }

    public static SubscriptionPlanUpdatedEvent create(
            Subscription subscription,
            String previousPlan,
            String currentPlan,
            String correlationId
    ) {
        return new SubscriptionPlanUpdatedEvent(
                UUID.randomUUID(),
                TYPE,
                SCHEMA_VERSION,
                Instant.now(),
                PRODUCER_NAME,
                correlationId == null || correlationId.isBlank()
                        ? UUID.randomUUID().toString() : correlationId,
                new Data(
                        subscription.getSubscriberType() == SubscriberType.STORE ? "STORE" : "BUYER",
                        previousPlan,
                        currentPlan));
    }

    public static String planCode(Subscription subscription) {
        return planCode(subscription.getSubscriberType(), subscription.getPlanId());
    }

    public static String planCode(SubscriberType type, Long planId) {
        if (type == SubscriberType.STORE) {
            return "PLAN_STORE_MONTHLY";
        }
        return planId != null && planId.equals(2L) ? "PLAN_RADAR_PLUS" : "PLAN_RADAR";
    }

    public static String previousPlanForActivation(Subscription subscription, SubscriptionStatus previousStatus) {
        if (previousStatus == SubscriptionStatus.SUSPENDED) {
            return SUSPENDED;
        }
        if (subscription.getSubscriberType() == SubscriberType.USER
                && "PLAN_RADAR_PLUS".equals(planCode(subscription))) {
            return "PLAN_RADAR";
        }
        return null;
    }
}
