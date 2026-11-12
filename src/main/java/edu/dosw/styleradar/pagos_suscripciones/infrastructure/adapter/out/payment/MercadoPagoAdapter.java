package edu.dosw.styleradar.pagos_suscripciones.infrastructure.adapter.out.payment;

import edu.dosw.styleradar.pagos_suscripciones.domain.model.OrdenPago;
import edu.dosw.styleradar.pagos_suscripciones.domain.model.Suscripcion;
import edu.dosw.styleradar.pagos_suscripciones.domain.port.out.MercadoPagoPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@Profile("!dev & !test")
@RequiredArgsConstructor
public class MercadoPagoAdapter implements MercadoPagoPort {

    private static final String KEY_INIT_POINT = "init_point";
    private static final String KEY_PREAPPROVAL_ID = "preapproval_id";
    private static final String MP_KEY_ID = "id";

    private final MercadoPagoFeignClient feignClient;

    @Value("${mercadopago.access-token}")
    private String accessToken;

    @Value("${mercadopago.webhook-url}")
    private String webhookUrl;

    private String auth() { return "Bearer " + accessToken; }

    @Override
    public Map<String, String> crearPreferenciaPago(OrdenPago orden, Suscripcion suscripcion, String emailPayer) {
        Map<String, Object> req = new HashMap<>();
        req.put("reason", "Suscripción StyleRadar - Plan " + suscripcion.getPlanId());
        req.put("external_reference", orden.getId().toString());
        req.put("payer_email", emailPayer);
        req.put("status", "pending");
        req.put("notification_url", webhookUrl);
        
        // CAMBIO AQUÍ: Redirigir a Swagger en lugar de styleradar.com que no existe
        req.put("back_url", "https://ms-pagos-qa.happyriver-65727909.northcentralus.azurecontainerapps.io/swagger-ui/index.html");
        
        req.put("auto_recurring", Map.of(
                "frequency", 1,
                "frequency_type", "months",
                "transaction_amount", orden.getMonto().valor(),
                "currency_id", orden.getMonto().moneda()));

        Map<String, Object> resp = feignClient.crearPreapproval(auth(), req);

        if (resp == null) {
            log.warn("MP devolvió null para preapproval de la orden {}", orden.getId());
            return Map.of(KEY_INIT_POINT, "", KEY_PREAPPROVAL_ID, "");
        }

        String initPoint = (String) resp.get(KEY_INIT_POINT);
        String preapprovalId = (String) resp.get(MP_KEY_ID);
        return Map.of(
            KEY_INIT_POINT, initPoint != null ? initPoint : "",
            KEY_PREAPPROVAL_ID, preapprovalId != null ? preapprovalId : ""
        );
    }

    @Override
    public void cancelarPreapproval(String preapprovalId) {
        feignClient.cancelarPreapproval(auth(), preapprovalId);
    }

    @Override
    public void reembolsarPago(String idPagoExterno, String monto) {
        log.info("Reembolso real a MP para pago {}: {}", idPagoExterno, monto);
        Map<String, Object> req = Map.of("amount", Double.parseDouble(monto));
        feignClient.reembolsarPago(auth(), idPagoExterno, req);
    }

    @Override
    public String consultarEstadoPago(String idPagoExterno) {
        Map<String, Object> pago = consultarPago(idPagoExterno);
        return pago != null && pago.get("status") != null ? pago.get("status").toString() : null;
    }

    @Override
    public Map<String, Object> consultarPago(String idPagoExterno) {
        try {
            return feignClient.consultarPago(auth(), idPagoExterno);
        } catch (Exception e) {
            log.error("Error consultando pago {} en MP (posiblemente ID falso de prueba): {}", idPagoExterno, e.getMessage());
            return null;
        }
    }
}