package edu.dosw.styleradar.pagos_suscripciones.domain.port.out;

import edu.dosw.styleradar.pagos_suscripciones.domain.model.OrdenPago;
import edu.dosw.styleradar.pagos_suscripciones.domain.model.Suscripcion;
import java.util.Map;

public interface MercadoPagoPort {
    /**
     * Crea una preferencia de pago en Mercado Pago.
     * @return Un mapa con el 'init_point' y el 'preapproval_id'.
     */
    Map<String, String> crearPreferenciaPago(OrdenPago orden, Suscripcion suscripcion, String emailPayer);
    void cancelarPreapproval(String preapprovalId);
    void reembolsarPago(String idPagoExterno, String monto);
    /** Consulta el estado real (approved/rejected/pending/refunded) del pago en MP. */
    String consultarEstadoPago(String idPagoExterno);

    Map<String, Object> consultarPago(String idPagoExterno);
    Map<String, Object> consultarPagoAutorizado(String idPagoAutorizado);
}