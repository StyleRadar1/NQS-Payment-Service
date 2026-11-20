package edu.dosw.styleradar.payments.domain.exception;

public class PaymentOrderNotFoundException extends ResourceNotFoundException {

    public PaymentOrderNotFoundException() {
        super("PAYMENT_ORDER_NOT_FOUND", "No se encontró la orden de pago solicitada.");
    }

    public PaymentOrderNotFoundException(String message) {
        super("PAYMENT_ORDER_NOT_FOUND", message);
    }
}

