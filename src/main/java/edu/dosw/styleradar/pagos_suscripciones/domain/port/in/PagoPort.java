package edu.dosw.styleradar.pagos_suscripciones.domain.port.in;

public interface PagoPort {
    void reembolsarPago(Long ordenPagoId, String motivo);
}