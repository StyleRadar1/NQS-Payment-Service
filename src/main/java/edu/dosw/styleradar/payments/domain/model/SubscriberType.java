package edu.dosw.styleradar.payments.domain.model;

public enum SubscriberType {
    USER("USUARIO"),
    STORE("ALMACEN");

    private final String legacyValue;

    SubscriberType(String legacyValue) {
        this.legacyValue = legacyValue;
    }

    public String legacyValue() {
        return legacyValue;
    }
}
