package edu.dosw.styleradar.payments.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
class SubscriptionTest {

    private static final LocalDate HOY = LocalDate.of(2026, 11, 14);
    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 11, 1, 10, 0);

    private Subscription pending() {
        return Subscription.pending("USER-1", SubscriberType.USER, 2L, AHORA);
    }

    private Subscription active(LocalDate inicio, LocalDate fin) {
        return Subscription.rehydrate(10L, "USER-1", SubscriberType.USER, 2L, null,
                SubscriptionStatus.ACTIVE, inicio, fin, true, "PRE-1", null, AHORA);
    }

    @Test
    void nacePendingSinDates() {
        Subscription subscription = pending();

        assertThat(subscription.getStatus()).isEqualTo(SubscriptionStatus.PENDING);
        assertThat(subscription.getStartDate()).isNull();
        assertThat(subscription.getEndDate()).isNull();
        assertThat(subscription.isAutoRenewal()).isTrue();
        assertThat(subscription.getId()).isNull();
        assertThat(subscription.isPending()).isTrue();
    }

    @Test
    void validaDataObligatorios() {
        assertThatThrownBy(() -> Subscription.pending(" ", SubscriberType.USER, 2L, AHORA))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("suscriptor");
        assertThatThrownBy(() -> Subscription.pending("USER-1", null, 2L, AHORA))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("tipo");
        assertThatThrownBy(() -> Subscription.pending("USER-1", SubscriberType.USER, null, AHORA))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("plan");
    }

    @Test
    void asignaPreapprovalYNit() {
        Subscription subscription = pending();

        subscription.assignPreapproval("PRE-123");
        subscription.assignTaxId("900123456");

        assertThat(subscription.getMercadoPagoPreapprovalId()).isEqualTo("PRE-123");
        assertThat(subscription.getTaxId()).isEqualTo("900123456");
    }

    @Test
    void normalizaNitConDigitoDeVerificacion() {
        Subscription subscription = pending();
        subscription.assignTaxId("900123456-1");
        assertThat(subscription.getTaxId()).isEqualTo("900123456");
    }

    @Test
    void asignaNitVacioComoNulo() {
        Subscription subscription = pending();
        subscription.assignTaxId("900123456");
        subscription.assignTaxId(" ");

        assertThat(subscription.getTaxId()).isNull();
    }

    @Test
    void rechazaPreapprovalVaciaYNitLargo() {
        Subscription subscription = pending();

        assertThatThrownBy(() -> subscription.assignPreapproval(" "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("preaprobación");
        assertThatThrownBy(() -> subscription.assignTaxId("9".repeat(31)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("NIT");
    }

    @Nested
    class Activation {

        @Test
        void primeraActivationAsignaPeriodoMensual() {
            Subscription subscription = pending();

            subscription.activate(HOY);

            assertThat(subscription.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
            assertThat(subscription.getStartDate()).isEqualTo(HOY);
            assertThat(subscription.getEndDate()).isEqualTo(HOY.plusMonths(1));
            assertThat(subscription.isAutoRenewal()).isTrue();
        }

        @Test
        void renovacionVigenteExtiendeFromEndDate() {
            Subscription subscription = active(HOY.minusMonths(1), HOY);

            subscription.activate(HOY);

            assertThat(subscription.getEndDate()).isEqualTo(HOY.plusMonths(1));
            assertThat(subscription.getStartDate()).isEqualTo(HOY.minusMonths(1));
        }

        @Test
        void renovacionVencidaCuentaFromElPayment() {
            Subscription subscription = active(HOY.minusMonths(2), HOY.minusDays(3));

            subscription.activate(HOY);

            assertThat(subscription.getStartDate()).isEqualTo(HOY);
            assertThat(subscription.getEndDate()).isEqualTo(HOY.plusMonths(1));
        }

        @Test
        void reactivetionFromSuspendedCuentaFromElPayment() {
            Subscription subscription = Subscription.rehydrate(10L, "USER-1", SubscriberType.USER, 2L,
                    null, SubscriptionStatus.SUSPENDED, HOY.minusMonths(2), HOY.minusMonths(1),
                    false, "PRE-1", null, AHORA);

            subscription.activate(HOY);

            assertThat(subscription.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
            assertThat(subscription.getStartDate()).isEqualTo(HOY);
            assertThat(subscription.getEndDate()).isEqualTo(HOY.plusMonths(1));
            assertThat(subscription.isAutoRenewal()).isTrue();
        }

        @Test
        void rechazaDateNula() {
            assertThatThrownBy(() -> pending().activate(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    class CancellationSuspension {

        @Test
        void cancelaRenovacionDeUnaSubscriptionActive() {
            Subscription subscription = active(HOY.minusDays(10), HOY.plusDays(20));

            subscription.cancelRenewal();

            assertThat(subscription.isAutoRenewal()).isFalse();
            assertThat(subscription.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
            assertThat(subscription.getEndDate()).isEqualTo(HOY.plusDays(20));
        }

        @Test
        void noCancelaRenovacionDePendingOSuspended() {
            assertThatThrownBy(() -> pending().cancelRenewal())
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("activa");

            Subscription suspended = Subscription.rehydrate(10L, "USER-1", SubscriberType.USER, 2L,
                    null, SubscriptionStatus.SUSPENDED, HOY.minusMonths(2), HOY.minusMonths(1),
                    false, null, null, AHORA);
            assertThatThrownBy(suspended::cancelRenewal)
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        void suspendeCuandoElPeriodoYaVencio() {
            Subscription subscription = active(HOY.minusMonths(2), HOY.minusDays(1));

            subscription.suspendForNonPayment(HOY);

            assertThat(subscription.getStatus()).isEqualTo(SubscriptionStatus.SUSPENDED);
            assertThat(subscription.isAutoRenewal()).isFalse();
        }

        @Test
        void noSuspendeSiAunNoVence() {
            Subscription subscription = active(HOY.minusDays(10), HOY.plusDays(10));

            assertThatThrownBy(() -> subscription.suspendForNonPayment(HOY))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("vencido");
        }

        @Test
        void noSuspendeUnaSubscriptionNoActive() {
            assertThatThrownBy(() -> pending().suspendForNonPayment(HOY))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("activa");
            assertThatThrownBy(() -> pending().suspendForNonPayment(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    class Vigencia {

        @Test
        void activeConEndDateFuturaTieneAccess() {
            Subscription subscription = active(HOY.minusDays(10), HOY.plusDays(10));

            assertThat(subscription.isInForce(HOY)).isTrue();
            assertThat(subscription.allowsAccess(HOY)).isTrue();
        }

        @Test
        void laEndDateEsInclusiva() {
            Subscription subscription = active(HOY.minusMonths(1), HOY);

            assertThat(subscription.isInForce(HOY)).isTrue();
        }

        @Test
        void activeVencidaNoTieneAccess() {
            Subscription subscription = active(HOY.minusMonths(2), HOY.minusDays(1));

            assertThat(subscription.isInForce(HOY)).isFalse();
            assertThat(subscription.allowsAccess(HOY)).isFalse();
        }

        @Test
        void pendingYSuspendedNoTienenAccess() {
            assertThat(pending().isInForce(HOY)).isFalse();

            Subscription suspended = Subscription.rehydrate(10L, "USER-1", SubscriberType.STORE, 3L,
                    null, SubscriptionStatus.SUSPENDED, HOY.minusMonths(2), HOY.minusMonths(1),
                    false, null, "900123456", AHORA);
            assertThat(suspended.isInForce(HOY)).isFalse();
            assertThat(suspended.isSuspended()).isTrue();
            assertThat(suspended.isOfType(SubscriberType.STORE)).isTrue();
        }
    }

    @Test
    void rehidrataAllLosCampos() {
        Subscription subscription = Subscription.rehydrate(7L, "STORE-9", SubscriberType.STORE, 3L, 2L,
                SubscriptionStatus.ACTIVE, HOY.minusDays(5), HOY.plusDays(25), false,
                "PRE-9", "900123456", AHORA);

        assertThat(subscription.getId()).isEqualTo(7L);
        assertThat(subscription.getSubscriberId()).isEqualTo("STORE-9");
        assertThat(subscription.getSubscriberType()).isEqualTo(SubscriberType.STORE);
        assertThat(subscription.getPlanId()).isEqualTo(3L);
        assertThat(subscription.getScheduledPlanId()).isEqualTo(2L);
        assertThat(subscription.getTaxId()).isEqualTo("900123456");
        assertThat(subscription.getMercadoPagoPreapprovalId()).isEqualTo("PRE-9");
        assertThat(subscription.getUpdatedAt()).isEqualTo(AHORA);
        assertThat(subscription.isActive()).isTrue();
    }

    @Test
    void markUpdatedCambiaLaDate() {
        Subscription subscription = pending();
        LocalDateTime antes = subscription.getUpdatedAt();

        subscription.markUpdated();

        assertThat(subscription.getUpdatedAt()).isAfterOrEqualTo(antes);
    }
}
