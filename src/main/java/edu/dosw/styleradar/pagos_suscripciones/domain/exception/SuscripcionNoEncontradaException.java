package edu.dosw.styleradar.pagos_suscripciones.domain.exception;

public class SuscripcionNoEncontradaException extends RuntimeException {
    public SuscripcionNoEncontradaException(Long id) {
        super("No se encontró la suscripción con ID: " + id);
    }
    public SuscripcionNoEncontradaException(String message) {
        super(message);
    }
}