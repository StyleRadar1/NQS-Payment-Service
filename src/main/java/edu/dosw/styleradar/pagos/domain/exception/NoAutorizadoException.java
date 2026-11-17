package edu.dosw.styleradar.pagos.domain.exception;

/** Falta de autenticación o identidad inválida (HTTP 401). */
public class NoAutorizadoException extends PagosException {

    public NoAutorizadoException(String mensaje) {
        super("UNAUTHORIZED", mensaje);
    }
}
