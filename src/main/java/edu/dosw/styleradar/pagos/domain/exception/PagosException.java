package edu.dosw.styleradar.pagos.domain.exception;

/**
 * Excepción base del dominio de pagos. El código se usa en el campo
 * {@code error} de {@code ErrorResponse} (ADR-10).
 */
public abstract class PagosException extends RuntimeException {

    private final String codigo;

    protected PagosException(String codigo, String mensaje) {
        super(mensaje);
        this.codigo = codigo;
    }

    public String getCodigo() {
        return codigo;
    }
}
