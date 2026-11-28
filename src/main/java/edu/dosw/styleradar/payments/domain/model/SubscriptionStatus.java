package edu.dosw.styleradar.payments.domain.model;

public enum SubscriptionStatus {
    PENDING("PENDIENTE"),
    ACTIVE("ACTIVA"),
    SUSPENDED("SUSPENDIDA");

    private final String legacyValue;

    SubscriptionStatus(String legacyValue) {
        this.legacyValue = legacyValue;
    }

    public String legacyValue() {
        return legacyValue;
    }
}
