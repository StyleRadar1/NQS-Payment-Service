package edu.dosw.styleradar.payments.domain.exception;

public class InvalidRequestException extends PaymentsException {

    public InvalidRequestException(String message) {
        super("BAD_REQUEST", message);
    }
}
