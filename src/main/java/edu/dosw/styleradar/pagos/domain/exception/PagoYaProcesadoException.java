package edu.dosw.styleradar.pagos.domain.exception;

/** El pago ya fue procesado o reembolsado (HTTP 409). */
public class PagoYaProcesadoException extends ConflictoException {

    public PagoYaProcesadoException() {
        super("PAGO_YA_PROCESADO", "El pago ya fue procesado.");
    }

    public PagoYaProcesadoException(String mensaje) {
        super("PAGO_YA_PROCESADO", mensaje);
    }
}
