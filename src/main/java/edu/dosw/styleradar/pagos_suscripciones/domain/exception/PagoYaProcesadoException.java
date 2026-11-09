package edu.dosw.styleradar.pagos_suscripciones.domain.exception;

public class PagoYaProcesadoException extends RuntimeException {
    public PagoYaProcesadoException(String idPagoExterno) {
        super("El pago con ID externo " + idPagoExterno + " ya fue procesado (Idempotencia)");
    }
}