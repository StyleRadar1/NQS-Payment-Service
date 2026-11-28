package edu.dosw.styleradar.payments.domain.exception;

public class ResourceNotFoundException extends PaymentsException {

    public ResourceNotFoundException(String code, String message) {
        super(code, message);
    }
}
