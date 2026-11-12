package edu.dosw.styleradar.pagos_suscripciones.domain.port.out;

public interface IdempotencyPort {
    boolean adquirirLock(String idPagoExterno);
    void liberarLock(String idPagoExterno);
}