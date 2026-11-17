package edu.dosw.styleradar.pagos.application.dto.request;

import java.util.Map;

/**
 * Notificación normalizada de Mercado Pago.
 *
 * <p>SR-RF-94: solo se procesan notificaciones de pagos; los avisos de
 * preapproval no modifican el estado de un pago (RN-04).</p>
 */
public record NotificacionPago(
        String idPago,
        String tipo,
        String requestId,
        Map<String, Object> payload) {
}
