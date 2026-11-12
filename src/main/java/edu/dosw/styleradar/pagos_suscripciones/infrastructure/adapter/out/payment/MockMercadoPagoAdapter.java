package edu.dosw.styleradar.pagos_suscripciones.infrastructure.adapter.out.payment;

import edu.dosw.styleradar.pagos_suscripciones.domain.model.OrdenPago;
import edu.dosw.styleradar.pagos_suscripciones.domain.model.Suscripcion;
import edu.dosw.styleradar.pagos_suscripciones.domain.port.out.MercadoPagoPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@Profile({"dev", "test"})
public class MockMercadoPagoAdapter implements MercadoPagoPort {

    @Override
    public Map<String, String> crearPreferenciaPago(OrdenPago orden, Suscripcion suscripcion, String emailPayer) {
        log.info("🧪 MOCK: creación de pago para plan {}", suscripcion.getPlanId());
        return Map.of(
            "init_point", "https://www.mercadopago.com.co/sandbox/checkout/v1/redirect?pref_id=MOCK-" + orden.getId(),
            "preapproval_id", "MOCK-PREAPPROVAL-" + orden.getId()
        );
    }

    @Override
    public void cancelarPreapproval(String preapprovalId) {
        log.info("🧪 MOCK: cancel preapproval {}", preapprovalId);
    }

    @Override
    public void reembolsarPago(String idPagoExterno, String monto) {
        log.info("🧪 MOCK: reembolso {} para pago {}", monto, idPagoExterno);
    }

    @Override
    public String consultarEstadoPago(String idPagoExterno) {
        log.info("🧪 MOCK: consultando estado de pago {} → approved", idPagoExterno);
        return "approved";
    }

    @Override
    public Map<String, Object> consultarPago(String idPagoExterno) {
        log.info("🧪 MOCK: consultando pago completo {} → approved con external_reference 1", idPagoExterno);
        return Map.of("status", "approved", "external_reference", "1");
    }
}