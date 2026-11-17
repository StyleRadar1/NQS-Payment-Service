package edu.dosw.styleradar.pagos.application.port.in;

import edu.dosw.styleradar.pagos.application.dto.request.NotificacionPago;

/** Puerto de entrada para las notificaciones de Mercado Pago (SR-RF-94). */
public interface WebhookPort {

    void procesar(NotificacionPago notificacion, String correlationId);
}
