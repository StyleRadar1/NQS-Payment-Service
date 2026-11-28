package edu.dosw.styleradar.payments.domain.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

public final class Subscription {

    private Long id;
    private final String subscriberId;
    private final SubscriberType subscriberType;
    private final Long planId;
    private final Long scheduledPlanId;
    private SubscriptionStatus status;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean autoRenewal;
    private String mercadoPagoPreapprovalId;
    private String taxId;
    private java.util.UUID storeId;
    private LocalDateTime updatedAt;

    private Subscription(Long id,
                        String subscriberId,
                        SubscriberType subscriberType,
                        Long planId,
                        Long scheduledPlanId,
                        SubscriptionStatus status,
                        LocalDate startDate,
                        LocalDate endDate,
                        boolean autoRenewal,
                        String mercadoPagoPreapprovalId,
                        String taxId,
                        java.util.UUID storeId,
                        LocalDateTime updatedAt) {
        if (subscriberId == null || subscriberId.isBlank()) {
            throw new IllegalArgumentException("El suscriptor es obligatorio.");
        }
        this.id = id;
        this.subscriberId = subscriberId.trim();
        this.subscriberType = Objects.requireNonNull(subscriberType, "El tipo de suscriptor es obligatorio.");
        this.planId = Objects.requireNonNull(planId, "El plan es obligatorio.");
        this.scheduledPlanId = scheduledPlanId;
        this.status = Objects.requireNonNull(status, "El estado de la suscripción es obligatorio.");
        this.startDate = startDate;
        this.endDate = endDate;
        this.autoRenewal = autoRenewal;
        this.mercadoPagoPreapprovalId = mercadoPagoPreapprovalId;
        this.taxId = taxId;
        this.storeId = storeId;
        this.updatedAt = updatedAt == null ? LocalDateTime.now() : updatedAt;
    }

    public static Subscription pending(String subscriberId,
                                        SubscriberType subscriberType,
                                        Long planId,
                                        LocalDateTime ahora) {
        return new Subscription(null, subscriberId, subscriberType, planId, null,
                SubscriptionStatus.PENDING, null, null, true, null, null, null, ahora);
    }

    public static Subscription rehydrate(Long id,
                                         String subscriberId,
                                         SubscriberType subscriberType,
                                         Long planId,
                                         Long scheduledPlanId,
                                         SubscriptionStatus status,
                                         LocalDate startDate,
                                         LocalDate endDate,
                                         boolean autoRenewal,
                                         String mercadoPagoPreapprovalId,
                                         String taxId,
                                         LocalDateTime updatedAt) {
        return rehydrate(id, subscriberId, subscriberType, planId, scheduledPlanId, status,
                startDate, endDate, autoRenewal, mercadoPagoPreapprovalId, taxId, null, updatedAt);
    }

    public static Subscription rehydrate(Long id,
                                         String subscriberId,
                                         SubscriberType subscriberType,
                                         Long planId,
                                         Long scheduledPlanId,
                                         SubscriptionStatus status,
                                         LocalDate startDate,
                                         LocalDate endDate,
                                         boolean autoRenewal,
                                         String mercadoPagoPreapprovalId,
                                         String taxId,
                                         java.util.UUID storeId,
                                         LocalDateTime updatedAt) {
        return new Subscription(id, subscriberId, subscriberType, planId, scheduledPlanId,
                status, startDate, endDate, autoRenewal, mercadoPagoPreapprovalId, taxId, storeId,
                updatedAt);
    }

    
    public void assignPreapproval(String preapprovalId) {
        if (preapprovalId == null || preapprovalId.isBlank()) {
            throw new IllegalArgumentException("El identificador de la preaprobación es obligatorio.");
        }
        this.mercadoPagoPreapprovalId = preapprovalId.trim();
        this.updatedAt = LocalDateTime.now();
    }

    
    public void assignTaxId(String taxId) {
        if (taxId == null || taxId.isBlank()) {
            this.taxId = null;
            return;
        }
        String normalizado = taxId.trim();
        if (!normalizado.matches("\\d{9}(-\\d)?")) {
            throw new IllegalArgumentException("El NIT debe tener exactamente 9 dígitos.");
        }
        this.taxId = normalizado.substring(0, 9);
        this.updatedAt = LocalDateTime.now();
    }

    
    public void assignStoreId(java.util.UUID storeId) {
        this.storeId = storeId;
        this.updatedAt = LocalDateTime.now();
    }

    
    public void activate(LocalDate datePayment) {
        Objects.requireNonNull(datePayment, "La fecha del pago es obligatoria.");
        boolean current = status == SubscriptionStatus.ACTIVE
                && endDate != null
                && !endDate.isBefore(datePayment);
        if (current) {
            this.endDate = this.endDate.plusMonths(1);
        } else {
            this.startDate = datePayment;
            this.endDate = datePayment.plusMonths(1);
        }
        this.status = SubscriptionStatus.ACTIVE;
        this.autoRenewal = true;
        this.updatedAt = LocalDateTime.now();
    }

    
    public void cancelRenewal() {
        if (status != SubscriptionStatus.ACTIVE) {
            throw new IllegalStateException("Solo se puede cancelar la renovación de una suscripción activa.");
        }
        this.autoRenewal = false;
        this.updatedAt = LocalDateTime.now();
    }

    
    public void suspendForNonPayment(LocalDate today) {
        Objects.requireNonNull(today, "La fecha de suspensión es obligatoria.");
        if (status != SubscriptionStatus.ACTIVE) {
            throw new IllegalStateException("Solo se puede suspender una suscripción activa.");
        }
        if (endDate != null && !endDate.isBefore(today)) {
            throw new IllegalStateException("La suscripción aún no ha vencido.");
        }
        this.status = SubscriptionStatus.SUSPENDED;
        this.autoRenewal = false;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isInForce(LocalDate today) {
        return status == SubscriptionStatus.ACTIVE
                && endDate != null
                && !endDate.isBefore(today);
    }

    
    public boolean allowsAccess(LocalDate today) {
        return isInForce(today);
    }

    public boolean isOfType(SubscriberType type) {
        return subscriberType == type;
    }

    public boolean isPending() {
        return status == SubscriptionStatus.PENDING;
    }

    public boolean isActive() {
        return status == SubscriptionStatus.ACTIVE;
    }

    public boolean isSuspended() {
        return status == SubscriptionStatus.SUSPENDED;
    }

    public void markUpdated() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getSubscriberId() {
        return subscriberId;
    }

    public SubscriberType getSubscriberType() {
        return subscriberType;
    }

    public Long getPlanId() {
        return planId;
    }

    public Long getScheduledPlanId() {
        return scheduledPlanId;
    }

    public SubscriptionStatus getStatus() {
        return status;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public boolean isAutoRenewal() {
        return autoRenewal;
    }

    public String getMercadoPagoPreapprovalId() {
        return mercadoPagoPreapprovalId;
    }

    public String getTaxId() {
        return taxId;
    }

    public java.util.UUID getStoreId() {
        return storeId;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}

