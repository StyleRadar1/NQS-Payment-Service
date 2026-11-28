package edu.dosw.styleradar.payments.infrastructure.adapter.out.payment;

import edu.dosw.styleradar.payments.application.port.out.MercadoPagoPort;
import edu.dosw.styleradar.payments.domain.model.Amount;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
class MockMercadoPagoAdapterTest {

    private final MockMercadoPagoAdapter adapter = new MockMercadoPagoAdapter();

    @Test
    void creaPreapprovalSimulada() {
        MercadoPagoPort.PreapprovalCreated creado = adapter.createPreapproval(
                new MercadoPagoPort.PreapprovalRequest("Radar+", "SUS-1",
                        "a@b.com", Amount.cop(BigDecimal.TEN), "https://app/back"));

        assertThat(creado.id()).startsWith("MP-PREAPPROVAL-");
        assertThat(creado.initPoint()).contains(creado.id());
        assertThat(creado.status()).isEqualTo("pending");
    }

    @Test
    void simulaStatusesDelPayment() {
        assertThat(adapter.getPayment("MP-APPROVED-1").status()).isEqualTo("approved");
        assertThat(adapter.getPayment("MP-REJECTED-1").status()).isEqualTo("rejected");
        assertThat(adapter.getPayment("MP-PENDING-1").status()).isEqualTo("pending");
    }
}
