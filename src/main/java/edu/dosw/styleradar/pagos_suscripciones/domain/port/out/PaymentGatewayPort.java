package edu.dosw.styleradar.pagos_suscripciones.domain.port.out;

import edu.dosw.styleradar.pagos_suscripciones.domain.model.OrdenPago;
import java.util.Map;

public interface PaymentGatewayPort {
    Map<String, String> crearPreferenciaPago(OrdenPago orden);
    void cancelarSuscripcionRecurrente(String preapprovalId);
}