package edu.dosw.styleradar.payments.application.usecase;

import edu.dosw.styleradar.payments.TestFixtures;
import edu.dosw.styleradar.payments.application.dto.response.PaymentResponse;
import edu.dosw.styleradar.payments.application.dto.response.RetryPaymentResponse;
import edu.dosw.styleradar.payments.application.port.out.MercadoPagoPort;
import edu.dosw.styleradar.payments.application.port.out.PaymentOrderRepositoryPort;
import edu.dosw.styleradar.payments.application.port.out.PlanRepositoryPort;
import edu.dosw.styleradar.payments.application.port.out.SubscriptionRepositoryPort;
import edu.dosw.styleradar.payments.domain.exception.Messages;
import edu.dosw.styleradar.payments.domain.exception.UnauthorizedException;
import edu.dosw.styleradar.payments.domain.exception.OperationNotAllowedException;
import edu.dosw.styleradar.payments.domain.exception.PaymentOrderNotFoundException;
import edu.dosw.styleradar.payments.domain.exception.PaymentProcessingException;
import edu.dosw.styleradar.payments.domain.exception.InvalidRequestException;
import edu.dosw.styleradar.payments.domain.exception.SubscriptionNotFoundException;
import edu.dosw.styleradar.payments.domain.model.PaymentStatus;
import edu.dosw.styleradar.payments.domain.model.PaymentOrder;
import edu.dosw.styleradar.payments.domain.model.Subscription;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentUseCaseTest {

    @Mock
    private PaymentOrderRepositoryPort paymentOrderRepository;
    @Mock
    private SubscriptionRepositoryPort subscriptionRepository;
    @Mock
    private PlanRepositoryPort planRepository;
    @Mock
    private MercadoPagoPort mercadoPago;

    private PaymentUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new PaymentUseCase(paymentOrderRepository, subscriptionRepository, planRepository, mercadoPago,
                TestFixtures.CLOCK, "https://app/back");
    }

    @Test
    void historyMapeaPaymentsYExigeIdentidad() {
        PaymentOrder order = TestFixtures.conIdOrder(TestFixtures.aprobada(100L, "14900"), 1L);
        when(paymentOrderRepository.findBySubscriber("USER-1")).thenReturn(List.of(order));
        when(subscriptionRepository.findById(100L)).thenReturn(Optional.of(
                TestFixtures.activeUser("USER-1", TestFixtures.HOY.minusDays(5), TestFixtures.HOY.plusDays(25))));
        when(planRepository.findById(2L)).thenReturn(Optional.of(TestFixtures.planRadarPlus()));

        List<PaymentResponse> history = useCase.history("USER-1");

        assertThat(history).hasSize(1);
        assertThat(history.get(0).id()).isEqualTo(1L);
        assertThat(history.get(0).subscriberId()).isEqualTo("USER-1");
        assertThat(history.get(0).subscriberType()).isEqualTo(edu.dosw.styleradar.payments.domain.model.SubscriberType.USER);
        assertThat(history.get(0).planId()).isEqualTo(2L);
        assertThat(history.get(0).planName()).isEqualTo("Radar+");
        assertThat(history.get(0).status()).isEqualTo(PaymentStatus.APPROVED);
        assertThatThrownBy(() -> useCase.history(" "))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Nested
    class Retry {

        private PaymentOrder rechazada() {
            PaymentOrder order = TestFixtures.pending(100L, "14900");
            order.updateStatus(PaymentStatus.REJECTED, "MP-1");
            return TestFixtures.conIdOrder(order, 10L);
        }

        private Subscription pendingConPreapproval() {
            Subscription subscription = TestFixtures.pendingUser("USER-1");
            subscription.assignPreapproval("PRE-OLD");
            return TestFixtures.conIdSubscription(subscription, 100L);
        }

        @Test
        void creaNuevaOrderYEnlace() {
            PaymentOrder rechazada = rechazada();
            Subscription subscription = pendingConPreapproval();
            when(paymentOrderRepository.findById(10L)).thenReturn(Optional.of(rechazada));
            when(subscriptionRepository.findById(100L)).thenReturn(Optional.of(subscription));
            when(paymentOrderRepository.save(any(PaymentOrder.class)))
                    .thenAnswer(inv -> TestFixtures.conIdOrder(inv.getArgument(0), 11L));
            when(mercadoPago.createPreapproval(any()))
                    .thenReturn(new MercadoPagoPort.PreapprovalCreated("PRE-NEW", "https://mp/new", "pending"));

            RetryPaymentResponse response = useCase.retry(10L, "pagador@testuser.com", "USER-1");

            assertThat(response.orderId()).isEqualTo(11L);
            assertThat(response.status()).isEqualTo(PaymentStatus.PENDING);
            assertThat(response.initPoint()).isEqualTo("https://mp/new");
            verify(subscriptionRepository).save(subscription);
            verify(mercadoPago).cancelPreapproval("PRE-OLD");
            assertThat(rechazada.getStatus()).isEqualTo(PaymentStatus.REJECTED);
        }

        @Test
        void fallaSiNoExisteOrderOSubscription() {
            when(paymentOrderRepository.findById(99L)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> useCase.retry(99L, "a@b.com", "USER-1"))
                    .isInstanceOf(PaymentOrderNotFoundException.class);

            when(paymentOrderRepository.findById(10L)).thenReturn(Optional.of(rechazada()));
            when(subscriptionRepository.findById(100L)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> useCase.retry(10L, "a@b.com", "USER-1"))
                    .isInstanceOf(SubscriptionNotFoundException.class);
        }

        @Test
        void exigeTitularidadYEmailValido() {
            when(paymentOrderRepository.findById(10L)).thenReturn(Optional.of(rechazada()));
            when(subscriptionRepository.findById(100L)).thenReturn(Optional.of(pendingConPreapproval()));

            assertThatThrownBy(() -> useCase.retry(10L, "a@b.com", "USER-2"))
                    .isInstanceOf(OperationNotAllowedException.class)
                    .hasMessage(Messages.OWNER_ONLY);
            assertThatThrownBy(() -> useCase.retry(10L, "a@b.com", null))
                    .isInstanceOf(UnauthorizedException.class);
            assertThatThrownBy(() -> useCase.retry(10L, "sin-email", "USER-1"))
                    .isInstanceOf(InvalidRequestException.class)
                    .hasMessage(Messages.INVALID_EMAIL);
        }

        @Test
        void noReintentaSiLaSubscriptionEstaActiveONoIsRejected() {
            Subscription active = TestFixtures.activeUser("USER-1",
                    TestFixtures.HOY.minusDays(10), TestFixtures.HOY.plusDays(20));
            when(paymentOrderRepository.findById(10L)).thenReturn(Optional.of(rechazada()));
            when(subscriptionRepository.findById(100L)).thenReturn(Optional.of(active));

            assertThatThrownBy(() -> useCase.retry(10L, "a@b.com", "USER-1"))
                    .isInstanceOf(InvalidRequestException.class)
                    .hasMessage(Messages.SUBSCRIPTION_ACTIVE_NO_RETRY);

            Subscription pending = TestFixtures.pendingUser("USER-1");
            pending.assignPreapproval("PRE-OLD");
            PaymentOrder aprobada = TestFixtures.conIdOrder(TestFixtures.aprobada(100L, "14900"), 10L);
            when(paymentOrderRepository.findById(10L)).thenReturn(Optional.of(aprobada));
            when(subscriptionRepository.findById(100L)).thenReturn(Optional.of(pending));

            assertThatThrownBy(() -> useCase.retry(10L, "a@b.com", "USER-1"))
                    .isInstanceOf(InvalidRequestException.class)
                    .hasMessage(Messages.ORDER_NOT_REJECTED);
        }

        @Test
        void envuelveFalloDeMercadoPago() {
            when(paymentOrderRepository.findById(10L)).thenReturn(Optional.of(rechazada()));
            when(subscriptionRepository.findById(100L)).thenReturn(Optional.of(pendingConPreapproval()));
            when(paymentOrderRepository.save(any(PaymentOrder.class)))
                    .thenAnswer(inv -> TestFixtures.conIdOrder(inv.getArgument(0), 11L));
            when(mercadoPago.createPreapproval(any())).thenThrow(new IllegalStateException("MP"));

            assertThatThrownBy(() -> useCase.retry(10L, "a@b.com", "USER-1"))
                    .isInstanceOf(PaymentProcessingException.class)
                    .hasMessage(Messages.RETRY_FAILED);
        }

        @Test
        void rechazaPreapprovalSinInitPoint() {
            when(paymentOrderRepository.findById(10L)).thenReturn(Optional.of(rechazada()));
            when(subscriptionRepository.findById(100L)).thenReturn(Optional.of(pendingConPreapproval()));
            when(paymentOrderRepository.save(any(PaymentOrder.class)))
                    .thenAnswer(inv -> TestFixtures.conIdOrder(inv.getArgument(0), 11L));
            when(mercadoPago.createPreapproval(any()))
                    .thenReturn(new MercadoPagoPort.PreapprovalCreated("PRE-NEW", null, "pending"));

            assertThatThrownBy(() -> useCase.retry(10L, "a@b.com", "USER-1"))
                    .isInstanceOf(PaymentProcessingException.class)
                    .hasMessage(Messages.RETRY_FAILED);
        }
    }
}
