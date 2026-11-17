package edu.dosw.styleradar.pagos.application.port.out;

import java.util.Map;

/** Puerto de salida para la auditoría de webhooks (SR-RNF-14). */
public interface WebhookLogPort {

    boolean existe(String idPagoExterno);

    void registrar(String idPagoExterno, Map<String, Object> payload, String estadoProcesamiento);
}
