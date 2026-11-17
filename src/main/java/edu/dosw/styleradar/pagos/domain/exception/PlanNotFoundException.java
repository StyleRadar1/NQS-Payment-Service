package edu.dosw.styleradar.pagos.domain.exception;

/** Plan inexistente o inactivo (HTTP 404). */
public class PlanNotFoundException extends RecursoNoEncontradoException {

    public PlanNotFoundException() {
        super("PLAN_NOT_FOUND", Mensajes.PLAN_NO_DISPONIBLE);
    }

    public PlanNotFoundException(String mensaje) {
        super("PLAN_NOT_FOUND", mensaje);
    }
}
