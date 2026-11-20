package edu.dosw.styleradar.payments.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

public final class PaymentOrder {

    private Long id;
    private final Long subscriptionId;
    private final Amount amount;
    private PaymentStatus status;
    private String externalPaymentId;
    private final LocalDateTime createdAt;

    private PaymentOrder(Long id,
                         Long subscriptionId,
                         Amount amount,
                         PaymentStatus status,
                         String externalPaymentId,
                         LocalDateTime createdAt) {
        this.id = id;
        this.subscriptionId = Objects.requireNonNull(subscriptionId, "La suscripción de la orden es obligatoria.");
        this.amount = Objects.requireNonNull(amount, "El monto de la orden es obligatorio.");
        this.status = Objects.requireNonNull(status, "El estado de la orden es obligatorio.");
        this.externalPaymentId = externalPaymentId;
        this.createdAt = createdAt == null ? LocalDateTime.now() : createdAt;
    }

    public static PaymentOrder pending(Long subscriptionId, Amount amount, LocalDateTime now) {
        return new PaymentOrder(null, subscriptionId, amount, PaymentStatus.PENDING, null, now);
    }

    public static PaymentOrder rehydrate(Long id,
                                         Long subscriptionId,
                                         Amount amount,
                                         PaymentStatus status,
                                         String externalPaymentId,
                                         LocalDateTime createdAt) {
        return new PaymentOrder(id, subscriptionId, amount, status, externalPaymentId, createdAt);
    }

    public void updateStatus(PaymentStatus newStatus, String externalPaymentId) {
        Objects.requireNonNull(newStatus, "El nuevo estado es obligatorio.");
        if (externalPaymentId != null && !externalPaymentId.isBlank()) {
            this.externalPaymentId = externalPaymentId.trim();
        }
        this.status = newStatus;
    }

    public boolean isPending() {
        return status == PaymentStatus.PENDING;
    }

    public boolean isApproved() {
        return status == PaymentStatus.APPROVED;
    }

    public boolean isRejected() {
        return status == PaymentStatus.REJECTED;
    }

    public Long getId() {
        return id;
    }

    public Long getSubscriptionId() {
        return subscriptionId;
    }

    public Amount getAmount() {
        return amount;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public String getExternalPaymentId() {
        return externalPaymentId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
