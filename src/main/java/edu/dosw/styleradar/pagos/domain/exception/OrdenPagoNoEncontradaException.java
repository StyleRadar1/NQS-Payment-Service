package edu.dosw.styleradar.pagos.domain.exception;

/** Orden de pago inexistente (HTTP 404). */
public class OrdenPagoNoEncontradaException extends RecursoNoEncontradoException {

    public OrdenPagoNoEncontradaException() {
        super("ORDEN_NOT_FOUND", "No se encontró la orden de pago solicitada.");
    }

    public OrdenPagoNoEncontradaException(String mensaje) {
        super("ORDEN_NOT_FOUND", mensaje);
    }
}
