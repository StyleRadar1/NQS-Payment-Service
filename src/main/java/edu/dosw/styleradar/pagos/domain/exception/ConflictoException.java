package edu.dosw.styleradar.pagos.domain.exception;

/** Conflicto de estado o idempotencia (HTTP 409). */
public class ConflictoException extends PagosException {

    public ConflictoException(String codigo, String mensaje) {
        super(codigo, mensaje);
    }
}
