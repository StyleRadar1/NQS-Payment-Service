package edu.dosw.styleradar.payments.infrastructure.adapter.out.payment;

import edu.dosw.styleradar.payments.application.port.out.MercadoPagoPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Component
@Profile({"dev", "test"})
public class MockMercadoPagoAdapter implements MercadoPagoPort {

    private final AtomicLong sequence = new AtomicLong(1000);

    @Override
    public PreapprovalCreated createPreapproval(PreapprovalRequest request) {
        String id = "MP-PREAPPROVAL-" + sequence.incrementAndGet();
        String initPoint = "https://www.mercadopago.com.co/subscriptions/checkout?preapproval_id="
                + id + "&activetion=true";
        log.info("Mock Mercado Pago: preapproval {} created for {} ({})",
                id, request.externalReference(), request.payerEmail());
        return new PreapprovalCreated(id, initPoint, "pending");
    }

    @Override
    public MercadoPagoPayment getPayment(String paymentId) {
        String status = simulatedStatus(paymentId);
        log.info("Mock Mercado Pago: payment {} queried with status {}", paymentId, status);
        return new MercadoPagoPayment(paymentId, status, null, null, new BigDecimal("14900.00"), "COP");
    }

    @Override
    public AuthorizedPayment getAuthorizedPayment(String authorizedPaymentId) {
        return null;
    }

    @Override
    public void cancelPreapproval(String preapprovalId) {
        log.info("Mock Mercado Pago: preapproval {} cancelled", preapprovalId);
    }

    private String simulatedStatus(String paymentId) {
        String id = paymentId == null ? "" : paymentId.toUpperCase(Locale.ROOT);
        if (id.contains("REJECTED")) {
            return "rejected";
        }
        if (id.contains("PENDING")) {
            return "pending";
        }
        return "approved";
    }
}
