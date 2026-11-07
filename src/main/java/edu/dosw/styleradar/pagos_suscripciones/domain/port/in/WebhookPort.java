package edu.dosw.styleradar.pagos_suscripciones.domain.port.in;

public interface WebhookPort {
    void procesarNotificacionPago(String payload, String firmaHmac);
}