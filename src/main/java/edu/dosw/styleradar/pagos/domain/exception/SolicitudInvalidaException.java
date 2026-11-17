package edu.dosw.styleradar.pagos.domain.exception;

/** Solicitud inválida por reglas de negocio o datos de entrada (HTTP 400). */
public class SolicitudInvalidaException extends PagosException {

    public SolicitudInvalidaException(String mensaje) {
        super("BAD_REQUEST", mensaje);
    }
}
