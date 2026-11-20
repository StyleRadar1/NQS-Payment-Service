package edu.dosw.styleradar.payments.application.port.out;

import edu.dosw.styleradar.payments.domain.model.Amount;

public interface MercadoPagoPort {

    PreapprovalCreated createPreapproval(PreapprovalRequest request);

    MercadoPagoPayment getPayment(String paymentId);

    void cancelPreapproval(String preapprovalId);

    record PreapprovalRequest(
            String reason,
            String externalReference,
            String payerEmail,
            Amount amount,
            String backUrl) {
    }

    record PreapprovalCreated(String id, String initPoint, String status) {
    }

    record MercadoPagoPayment(
            String id,
            String status,
            String externalReference,
            String preapprovalId,
            java.math.BigDecimal amount,
            String currency) {
    }
}
