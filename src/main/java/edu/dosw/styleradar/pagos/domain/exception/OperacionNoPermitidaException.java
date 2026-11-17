package edu.dosw.styleradar.pagos.domain.exception;

/** Operación no permitida para el estado o el rol del solicitante (HTTP 403). */
public class OperacionNoPermitidaException extends PagosException {

    public OperacionNoPermitidaException(String mensaje) {
        super("FORBIDDEN", mensaje);
    }
}
