package edu.dosw.styleradar.payments.application.service;

import edu.dosw.styleradar.payments.domain.model.PaymentStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
class PaymentStatusMapperTest {

    @Test
    void traduceStatusesApproveds() {
        assertThat(PaymentStatusMapper.fromMercadoPago("approved")).contains(PaymentStatus.APPROVED);
        assertThat(PaymentStatusMapper.fromMercadoPago("APPROVED")).contains(PaymentStatus.APPROVED);
    }

    @Test
    void traduceStatusisPendings() {
        assertThat(PaymentStatusMapper.fromMercadoPago("pending")).contains(PaymentStatus.PENDING);
        assertThat(PaymentStatusMapper.fromMercadoPago("in_process")).contains(PaymentStatus.PENDING);
        assertThat(PaymentStatusMapper.fromMercadoPago("authorized")).contains(PaymentStatus.PENDING);
        assertThat(PaymentStatusMapper.fromMercadoPago("in_mediation")).contains(PaymentStatus.PENDING);
    }

    @Test
    void traduceStatusesRejecteds() {
        assertThat(PaymentStatusMapper.fromMercadoPago("rejected")).contains(PaymentStatus.REJECTED);
        assertThat(PaymentStatusMapper.fromMercadoPago("cancelled")).contains(PaymentStatus.REJECTED);
        assertThat(PaymentStatusMapper.fromMercadoPago("canceled")).contains(PaymentStatus.REJECTED);
    }

    @Test
    void ignoresRefundedStatuses() {
        assertThat(PaymentStatusMapper.fromMercadoPago("refunded")).isEmpty();
        assertThat(PaymentStatusMapper.fromMercadoPago("charged_back")).isEmpty();
    }

    @Test
    void ignoraStatusesDesconocidos() {
        assertThat(PaymentStatusMapper.fromMercadoPago("otro")).isEmpty();
        assertThat(PaymentStatusMapper.fromMercadoPago("")).isEmpty();
        assertThat(PaymentStatusMapper.fromMercadoPago(null)).isEmpty();
    }
}

