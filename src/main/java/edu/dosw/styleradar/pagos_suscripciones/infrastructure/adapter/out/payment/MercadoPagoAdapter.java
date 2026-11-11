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
@Profile("!dev")
@RequiredArgsConstructor
public class MercadoPagoAdapter implements MercadoPagoPort {

    private final MercadoPagoFeignClient feignClient;

    @Value("${mercadopago.access-token}")
    private String accessToken;

    private String getAuthHeader() {
        return "Bearer " + accessToken;
    }

    @Override
    public Map<String, String> crearPreferenciaPago(OrdenPago orden, Suscripcion suscripcion, String emailPayer) {
        Map<String, Object> request = new HashMap<>();
        request.put("reason", "Suscripción StyleRadar - Plan " + suscripcion.getPlanId());
        request.put("payer_email", emailPayer);
        request.put("auto_recurring", Map.of(
                "frequency", 1,
                "frequency_type", "months",
                "transaction_amount", orden.getMonto().valor(),
                "currency_id", orden.getMonto().moneda()
        ));
        request.put("back_url", "https://styleradar.com/suscripcion/resultado");
        request.put("external_reference", suscripcion.getId().toString());

        Map<String, Object> response = feignClient.crearPreapproval(getAuthHeader(), request);
        
        String initPoint = (String) response.get("init_point");
        String preapprovalId = (String) response.get("id");
        
        return Map.of(
                "init_point", initPoint != null ? initPoint : "",
                "preapproval_id", preapprovalId != null ? preapprovalId : ""
        );
    }

    @Override
    public void cancelarPreapproval(String preapprovalId) {
        feignClient.cancelarPreapproval(getAuthHeader(), preapprovalId);
    }

    @Override
    public void reembolsarPago(String idPagoExterno, String monto) {
        log.info("Solicitando reembolso real a Mercado Pago para pago: {}", idPagoExterno);
        Map<String, Object> request = Map.of("amount", Double.parseDouble(monto));
        feignClient.reembolsarPago(getAuthHeader(), idPagoExterno, request);
    }
}