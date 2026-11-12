package edu.dosw.styleradar.pagos_suscripciones.domain.exception;

public class IdempotencyException extends RuntimeException {
    public IdempotencyException(String message) {
        super(message);
    }
}