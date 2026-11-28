package edu.dosw.styleradar.payments.domain.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PaymentOrderTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 11, 14, 10, 0);

    private PaymentOrder pending() {
        return PaymentOrder.pending(1L, Amount.cop(new BigDecimal("100.00")), NOW);
    }

    @Test
    void createsPendingOrderWithoutExternalPayment() {
        PaymentOrder order = pending();

        assertThat(order.getStatus()).isEqualTo(PaymentStatus.PENDING);
        assertThat(order.getExternalPaymentId()).isNull();
        assertThat(order.isPending()).isTrue();
        assertThat(order.getCreatedAt()).isEqualTo(NOW);
        assertThat(order.getId()).isNull();
    }

    @Test
    void validatesRequiredData() {
        assertThatThrownBy(() -> PaymentOrder.pending(null, Amount.cop(BigDecimal.TEN), NOW))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("suscripción");
        assertThatThrownBy(() -> PaymentOrder.pending(1L, null, NOW))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("monto");
    }

    @Test
    void updatesApprovedWithExternalId() {
        PaymentOrder order = pending();

        order.updateStatus(PaymentStatus.APPROVED, "MP-123");

        assertThat(order.getStatus()).isEqualTo(PaymentStatus.APPROVED);
        assertThat(order.getExternalPaymentId()).isEqualTo("MP-123");
        assertThat(order.isApproved()).isTrue();
    }

    @Test
    void updatesRejected() {
        PaymentOrder order = pending();

        order.updateStatus(PaymentStatus.REJECTED, "MP-456");

        assertThat(order.getStatus()).isEqualTo(PaymentStatus.REJECTED);
        assertThat(order.isRejected()).isTrue();
    }

    @Test
    void rehydratesAllFields() {
        PaymentOrder order = PaymentOrder.rehydrate(5L, 1L, Amount.cop(new BigDecimal("100.00")),
                PaymentStatus.APPROVED, "MP-1", NOW);

        assertThat(order.getId()).isEqualTo(5L);
        assertThat(order.getSubscriptionId()).isEqualTo(1L);
        assertThat(order.getExternalPaymentId()).isEqualTo("MP-1");
        assertThat(order.getCreatedAt()).isEqualTo(NOW);
    }

    @Test
    void rejectsNullStatus() {
        assertThatThrownBy(() -> pending().updateStatus(null, "MP-1"))
                .isInstanceOf(NullPointerException.class);
    }
}
