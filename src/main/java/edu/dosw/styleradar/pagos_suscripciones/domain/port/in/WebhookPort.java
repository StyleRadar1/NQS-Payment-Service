package edu.dosw.styleradar.pagos_suscripciones.domain.port.in;

public interface WebhookPort {
    /**
     * Procesa una notificación de pago (webhook).
     * @param idPagoExterno ID del pago en la pasarela.
     * @param estadoPago El nuevo estado del pago (APPROVED, REJECTED, etc.).
     * @param tipoEvento El tipo de evento notificado.
     */
    void procesarNotificacionPago(String idPagoExterno, String estadoPago, String tipoEvento);
}