package edu.dosw.styleradar.payments.domain.exception;

public class PaymentProcessingException extends PaymentsException {

    public PaymentProcessingException(String message) {
        super("PAYMENT_ERROR", message);
    }

    public PaymentProcessingException(String message, Throwable causa) {
        super("PAYMENT_ERROR", message);
        initCause(causa);
    }
}
