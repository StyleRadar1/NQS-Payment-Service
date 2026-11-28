package edu.dosw.styleradar.payments.domain.exception;

public abstract class PaymentsException extends RuntimeException {

    private final String code;

    protected PaymentsException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
