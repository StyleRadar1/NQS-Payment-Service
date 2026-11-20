package edu.dosw.styleradar.payments.domain.exception;

public class ConflictException extends PaymentsException {

    public ConflictException(String code, String message) {
        super(code, message);
    }
}
