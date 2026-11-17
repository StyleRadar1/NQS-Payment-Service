package edu.dosw.styleradar.pagos.domain.exception;

/** Notificación duplicada o en procesamiento (HTTP 409). */
public class IdempotencyException extends ConflictoException {

    public IdempotencyException() {
        super("IDEMPOTENCY", "La notificación ya está siendo procesada.");
    }

    public IdempotencyException(String mensaje) {
        super("IDEMPOTENCY", mensaje);
    }
}
