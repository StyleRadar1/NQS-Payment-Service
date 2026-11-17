package edu.dosw.styleradar.pagos.domain.exception;

/** Suscripción inexistente (HTTP 404). */
public class SuscripcionNoEncontradaException extends RecursoNoEncontradoException {

    public SuscripcionNoEncontradaException() {
        super("SUSCRIPCION_NOT_FOUND", "No se encontró la suscripción solicitada.");
    }

    public SuscripcionNoEncontradaException(String mensaje) {
        super("SUSCRIPCION_NOT_FOUND", mensaje);
    }
}
