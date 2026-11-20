package edu.dosw.styleradar.payments.domain.exception;

public class IdempotencyException extends ConflictException {

    public IdempotencyException() {
        super("IDEMPOTENCY", "La notificación ya está siendo procesada.");
    }

    public IdempotencyException(String message) {
        super("IDEMPOTENCY", message);
    }
}

