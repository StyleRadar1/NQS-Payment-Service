package edu.dosw.styleradar.pagos_suscripciones.domain.port.out;

import java.util.Map;

import edu.dosw.styleradar.pagos_suscripciones.domain.model.OrdenPago;
import edu.dosw.styleradar.pagos_suscripciones.domain.model.Suscripcion;

public interface MercadoPagoPort {
    // Retorna el init_point para redirigir al usuario
    Map<String, String> crearPreferenciaPago(OrdenPago orden, Suscripcion suscripcion);
    // Consulta el estado de un pago
    Map<String, String> consultarPago(String idPagoExterno);
    // Cancela una suscripción recurrente en MP
    void cancelarPreapproval(String preapprovalId);
    // Reembolsa un pago
    void reembolsarPago(String idPagoExterno, String monto);
}