package edu.dosw.styleradar.pagos.infrastructure.adapter.out.payment;

import edu.dosw.styleradar.pagos.application.port.out.MercadoPagoPort;
import edu.dosw.styleradar.pagos.domain.model.Monto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("MockMercadoPagoAdapter (ADR-05)")
class MockMercadoPagoAdapterTest {

    private final MockMercadoPagoAdapter adapter = new MockMercadoPagoAdapter();

    @Test
    void creaPreapprovalSimulada() {
        MercadoPagoPort.PreapprovalCreado creado = adapter.crearPreapproval(
                new MercadoPagoPort.PreapprovalSolicitud("Radar+", "SUS-1",
                        "a@b.com", Monto.cop(BigDecimal.TEN), "https://app/back"));

        assertThat(creado.id()).startsWith("MP-PREAPPROVAL-");
        assertThat(creado.initPoint()).contains(creado.id());
        assertThat(creado.estado()).isEqualTo("pending");
    }

    @Test
    void simulaEstadosDelPago() {
        assertThat(adapter.consultarPago("MP-APROBADO-1").estado()).isEqualTo("approved");
        assertThat(adapter.consultarPago("MP-RECHAZADO-1").estado()).isEqualTo("rejected");
        assertThat(adapter.consultarPago("MP-PENDIENTE-1").estado()).isEqualTo("pending");
        assertThat(adapter.consultarPago("MP-REEMBOLSADO-1").estado()).isEqualTo("refunded");
    }

    @Test
    void simulaReembolsoYcancelacion() {
        MercadoPagoPort.ReembolsoMp reembolso = adapter.reembolsar("MP-1", new BigDecimal("50.00"));

        assertThat(reembolso.id()).startsWith("MP-REFUND-");
        assertThat(reembolso.monto()).isEqualByComparingTo("50.00");

        adapter.cancelarPreapproval("PRE-1");
    }
}
