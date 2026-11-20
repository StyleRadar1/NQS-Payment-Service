package edu.dosw.styleradar.payments.domain.exception;

public class OperationNotAllowedException extends PaymentsException {

    public OperationNotAllowedException(String message) {
        super("FORBIDDEN", message);
    }
}
