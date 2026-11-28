package edu.dosw.styleradar.payments.domain.model;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class Plan {

    private final Long id;
    private final String name;
    private final SubscriberType subscriberType;
    private final Amount price;
    private final Map<String, Object> limits;
    private final boolean active;

    public Plan(Long id,
                String name,
                SubscriberType subscriberType,
                Amount price,
                Map<String, Object> limits,
                boolean active) {
        this.id = id;
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("El nombre del plan es obligatorio.");
        }
        this.name = name.trim();
        this.subscriberType = Objects.requireNonNull(subscriberType, "El tipo de suscriptor es obligatorio.");
        this.price = Objects.requireNonNull(price, "El precio del plan es obligatorio.");
        this.limits = limits == null
                ? Map.of()
                : Collections.unmodifiableMap(new LinkedHashMap<>(limits));
        this.active = active;
    }

    public static Plan free(Long id, String name) {
        return new Plan(id, name, SubscriberType.USER, Amount.cop(BigDecimal.ZERO), Map.of(), true);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public SubscriberType getSubscriberType() {
        return subscriberType;
    }

    public Amount getPrice() {
        return price;
    }

    public Map<String, Object> getLimits() {
        return limits;
    }

    public boolean isActive() {
        return active;
    }

    public boolean isFree() {
        return price.esZero();
    }

    public boolean esDePayment() {
        return !isFree();
    }

    public boolean isOfType(SubscriberType type) {
        return subscriberType == type;
    }
}
