package edu.dosw.styleradar.pagos.infrastructure.adapter.out.payment;

import edu.dosw.styleradar.pagos.application.port.out.MercadoPagoPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Adaptador simulado de Mercado Pago para desarrollo y pruebas (ADR-05).
 *
 * <p>No genera cobros reales. Para probar el webhook se puede usar un
 * identificador que contenga RECHAZADO, PENDIENTE o REEMBOLSADO; en caso
 * contrario el pago se considera APROBADO.</p>
 */
@Slf4j
@Component
@Profile({"dev", "test"})
public class MockMercadoPagoAdapter implements MercadoPagoPort {

    private final AtomicLong secuencia = new AtomicLong(1000);

    @Override
    public PreapprovalCreado crearPreapproval(PreapprovalSolicitud solicitud) {
        String id = "MP-PREAPPROVAL-" + secuencia.incrementAndGet();
        String initPoint = "https://www.mercadopago.com.co/subscriptions/checkout?preapproval_id="
                + id + "&activation=true";
        log.info("Mock Mercado Pago: preapproval {} creada para {} ({})",
                id, solicitud.referenciaExterna(), solicitud.correoPagador());
        return new PreapprovalCreado(id, initPoint, "pending");
    }

    @Override
    public PagoMp consultarPago(String idPago) {
        String estado = estadoSimulado(idPago);
        log.info("Mock Mercado Pago: pago {} consultado con estado {}", idPago, estado);
        return new PagoMp(idPago, estado, null, null, new BigDecimal("14900.00"), "COP");
    }

    @Override
    public ReembolsoMp reembolsar(String idPago, BigDecimal monto) {
        log.info("Mock Mercado Pago: reembolso de {} para el pago {}", monto, idPago);
        return new ReembolsoMp("MP-REFUND-" + secuencia.incrementAndGet(), monto);
    }

    @Override
    public void cancelarPreapproval(String idPreapproval) {
        log.info("Mock Mercado Pago: preapproval {} cancelada", idPreapproval);
    }

    private String estadoSimulado(String idPago) {
        String id = idPago == null ? "" : idPago.toUpperCase(Locale.ROOT);
        if (id.contains("RECHAZADO")) {
            return "rejected";
        }
        if (id.contains("PENDIENTE")) {
            return "pending";
        }
        if (id.contains("REEMBOLSADO")) {
            return "refunded";
        }
        return "approved";
    }
}
