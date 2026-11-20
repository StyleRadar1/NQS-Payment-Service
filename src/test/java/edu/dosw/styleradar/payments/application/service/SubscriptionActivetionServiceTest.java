package edu.dosw.styleradar.payments.application.service;

import edu.dosw.styleradar.payments.TestFixtures;
import edu.dosw.styleradar.payments.application.port.out.SubscriptionRepositoryPort;
import edu.dosw.styleradar.payments.domain.model.PaymentStatus;
import edu.dosw.styleradar.payments.domain.model.PaymentOrder;
import edu.dosw.styleradar.payments.domain.model.Subscription;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubscriptionActivationServiceTest {

    @Mock
    private SubscriptionRepositoryPort repositorio;

    private SubscriptionActivationService servicio;

    @BeforeEach
    void setUp() {
        servicio = new SubscriptionActivationService(repositorio);
    }

    @Test
    void activeYGuardaConPaymentApproved() {
        Subscription subscription = TestFixtures.pendingUser("USER-1");
        PaymentOrder order = TestFixtures.aprobada(100L, "14900");
        PaymentOrder orderConSubscription = TestFixtures.conIdOrder(order, 10L);
        Subscription conId = TestFixtures.conIdSubscription(subscription, 100L);
        PaymentOrder orderValida = PaymentOrder.rehydrate(10L, 100L, orderConSubscription.getAmount(),
                PaymentStatus.APPROVED, "MP-1", TestFixtures.AHORA);
        when(repositorio.save(any(Subscription.class))).thenAnswer(inv -> inv.getArgument(0));

        Subscription activeda = servicio.activate(conId, orderValida, TestFixtures.HOY);

        assertThat(activeda.getStatus().name()).isEqualTo("ACTIVE");
        assertThat(activeda.getEndDate()).isEqualTo(TestFixtures.HOY.plusMonths(1));
        verify(repositorio).save(conId);
    }

    @Test
    void exigePaymentApproved() {
        Subscription subscription = TestFixtures.pendingUser("USER-1");
        PaymentOrder pending = TestFixtures.pending(100L, "14900");

        assertThatThrownBy(() -> servicio.activate(subscription, pending, LocalDate.now()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("aprobado");
        verify(repositorio, never()).save(any());
    }

    @Test
    void exigeQueLaOrderPertenezcaALaSubscription() {
        Subscription subscription = TestFixtures.conIdSubscription(
                TestFixtures.pendingUser("USER-1"), 100L);
        PaymentOrder order = TestFixtures.aprobada(999L, "14900");

        assertThatThrownBy(() -> servicio.activate(subscription, order, LocalDate.now()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("pertenece");
    }
}
