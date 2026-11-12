package edu.dosw.styleradar.pagos_suscripciones.domain.exception;

public class PlanNotFoundException extends RuntimeException {
    public PlanNotFoundException(String message) {
        super(message);
    }
}