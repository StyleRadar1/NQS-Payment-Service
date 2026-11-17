package edu.dosw.styleradar.pagos.domain.exception;

/** Error al comunicarse con Mercado Pago o al procesar el pago (HTTP 422). */
public class PaymentProcessingException extends PagosException {

    public PaymentProcessingException(String mensaje) {
        super("PAYMENT_ERROR", mensaje);
    }

    public PaymentProcessingException(String mensaje, Throwable causa) {
        super("PAYMENT_ERROR", mensaje);
        initCause(causa);
    }
}
