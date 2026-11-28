package edu.dosw.styleradar.payments;

import edu.dosw.styleradar.payments.domain.model.PaymentStatus;
import edu.dosw.styleradar.payments.domain.model.SubscriptionStatus;
import edu.dosw.styleradar.payments.domain.model.Amount;
import edu.dosw.styleradar.payments.domain.model.PaymentOrder;
import edu.dosw.styleradar.payments.domain.model.Plan;
import edu.dosw.styleradar.payments.domain.model.Subscription;
import edu.dosw.styleradar.payments.domain.model.SubscriberType;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;

public final class TestFixtures {

    public static final LocalDate HOY = LocalDate.of(2026, 11, 14);
    public static final LocalDateTime AHORA = LocalDateTime.of(2026, 11, 14, 10, 0);
    public static final Clock CLOCK = Clock.fixed(
            LocalDateTime.of(2026, 11, 14, 10, 0).toInstant(ZoneOffset.UTC), ZoneOffset.UTC);

    private TestFixtures() {
    }

    public static Plan planRadarPlus() {
        return new Plan(2L, "Radar+", SubscriberType.USER,
                Amount.cop(new BigDecimal("14900.00")),
                Map.of("max_wishlist", -1, "probador_foto", true), true);
    }

    public static Plan planStore() {
        return new Plan(3L, "Almacén Básico", SubscriberType.STORE,
                Amount.cop(new BigDecimal("89000.00")),
                Map.of("max_productos", 50), true);
    }

    public static Subscription pendingUser(String subscriberId) {
        return Subscription.pending(subscriberId, SubscriberType.USER, 2L, AHORA);
    }

    public static Subscription pendingStore(String subscriberId, String nit) {
        Subscription subscription = Subscription.pending(subscriberId, SubscriberType.STORE, 3L, AHORA);
        subscription.assignTaxId(nit);
        return subscription;
    }

    public static Subscription activeUser(String subscriberId, LocalDate inicio, LocalDate fin) {
        return Subscription.rehydrate(100L, subscriberId, SubscriberType.USER, 2L, null,
                SubscriptionStatus.ACTIVE, inicio, fin, true, "PRE-100", null, AHORA);
    }

    public static Subscription activeStore(String subscriberId, String nit, LocalDate inicio, LocalDate fin) {
        return Subscription.rehydrate(200L, subscriberId, SubscriberType.STORE, 3L, null,
                SubscriptionStatus.ACTIVE, inicio, fin, true, "PRE-200", nit, AHORA);
    }

    public static Subscription suspendedUser(String subscriberId) {
        return Subscription.rehydrate(101L, subscriberId, SubscriberType.USER, 2L, null,
                SubscriptionStatus.SUSPENDED, HOY.minusMonths(2), HOY.minusMonths(1),
                false, "PRE-101", null, AHORA);
    }

    public static PaymentOrder pending(Long subscriptionId, String value) {
        return PaymentOrder.pending(subscriptionId, Amount.cop(new BigDecimal(value)), AHORA);
    }

    public static PaymentOrder aprobada(Long subscriptionId, String value) {
        PaymentOrder order = pending(subscriptionId, value);
        order.updateStatus(PaymentStatus.APPROVED, "MP-1");
        return order;
    }

    public static Subscription conIdSubscription(Subscription subscription, Long id) {
        return Subscription.rehydrate(id, subscription.getSubscriberId(), subscription.getSubscriberType(),
                subscription.getPlanId(), subscription.getScheduledPlanId(), subscription.getStatus(),
                subscription.getStartDate(), subscription.getEndDate(), subscription.isAutoRenewal(),
                subscription.getMercadoPagoPreapprovalId(), subscription.getTaxId(), subscription.getUpdatedAt());
    }

    public static PaymentOrder conIdOrder(PaymentOrder order, Long id) {
        return PaymentOrder.rehydrate(id, order.getSubscriptionId(), order.getAmount(), order.getStatus(),
                order.getExternalPaymentId(), order.getCreatedAt());
    }
}
