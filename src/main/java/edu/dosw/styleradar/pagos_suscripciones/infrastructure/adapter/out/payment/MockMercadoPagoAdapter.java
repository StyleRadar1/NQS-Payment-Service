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
@Profile("dev") // <--- Solo se activa en desarrollo
public class MockMercadoPagoAdapter implements MercadoPagoPort {

    @Override
    public Map<String, String> crearPreferenciaPago(OrdenPago orden, Suscripcion suscripcion, String emailPayer) {
        log.info("🧪 MOCK: Simulando creación de pago para el plan {}", suscripcion.getPlanId());
        return Map.of(
                "init_point", "https://www.mercadopago.com.co/sandbox/checkout/v1/redirect?pref_id=MOCK-" + orden.getIdPagoExterno(),
                "preapproval_id", "MOCK-PREAPPROVAL-" + orden.getIdPagoExterno()
        );
    }

    @Override
    public void cancelarPreapproval(String preapprovalId) {
        log.info("🧪 MOCK: Cancelando preapproval en Mercado Pago con ID: {}", preapprovalId);
    }

    @Override
    public void reembolsarPago(String idPagoExterno, String monto) {
        log.info("🧪 MOCK: Simulando reembolso a Mercado Pago de ${} para el pago {}", monto, idPagoExterno);
    }
}