package edu.dosw.styleradar.pagos.domain.exception;

/** Recurso no encontrado (HTTP 404). */
public class RecursoNoEncontradoException extends PagosException {

    public RecursoNoEncontradoException(String codigo, String mensaje) {
        super(codigo, mensaje);
    }
}
