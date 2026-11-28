package edu.dosw.styleradar.payments.application.dto.response;

import edu.dosw.styleradar.payments.domain.model.Plan;
import edu.dosw.styleradar.payments.domain.model.SubscriberType;

import java.util.Map;

public record PlanResponse(
        Long id,
        String name,
        SubscriberType subscriberType,
        AmountResponse price,
        Map<String, Object> limits,
        boolean active) {

    public static PlanResponse from(Plan plan) {
        return new PlanResponse(
                plan.getId(),
                plan.getName(),
                plan.getSubscriberType(),
                AmountResponse.from(plan.getPrice()),
                plan.getLimits(),
                plan.isActive());
    }
}
