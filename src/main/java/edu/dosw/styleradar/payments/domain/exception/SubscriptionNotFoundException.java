package edu.dosw.styleradar.payments.domain.exception;

public class SubscriptionNotFoundException extends ResourceNotFoundException {

    public SubscriptionNotFoundException() {
        super("SUBSCRIPTION_NOT_FOUND", "No se encontró la suscripción solicitada.");
    }

    public SubscriptionNotFoundException(String message) {
        super("SUBSCRIPTION_NOT_FOUND", message);
    }
}

