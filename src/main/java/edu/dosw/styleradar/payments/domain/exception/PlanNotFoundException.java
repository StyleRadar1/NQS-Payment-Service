package edu.dosw.styleradar.payments.domain.exception;

public class PlanNotFoundException extends ResourceNotFoundException {

    public PlanNotFoundException() {
        super("PLAN_NOT_FOUND", Messages.PLAN_NOT_AVAILABLE);
    }

    public PlanNotFoundException(String message) {
        super("PLAN_NOT_FOUND", message);
    }
}
