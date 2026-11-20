package edu.dosw.styleradar.payments.domain.exception;

public class UnauthorizedException extends PaymentsException {

    public UnauthorizedException(String message) {
        super("UNAUTHORIZED", message);
    }
}
