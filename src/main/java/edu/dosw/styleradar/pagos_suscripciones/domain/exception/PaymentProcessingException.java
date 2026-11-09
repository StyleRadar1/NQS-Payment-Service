package edu.dosw.styleradar.pagos_suscripciones.domain.exception;

public class PaymentProcessingException extends RuntimeException {
    public PaymentProcessingException(String message) {
        super(message);
    }
}