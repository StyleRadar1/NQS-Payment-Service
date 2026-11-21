package edu.dosw.styleradar.payments.application.usecase;

import edu.dosw.styleradar.payments.TestFixtures;
import edu.dosw.styleradar.payments.application.dto.request.NotificationPayment;
import edu.dosw.styleradar.payments.application.port.out.EventPublisherPort;
import edu.dosw.styleradar.payments.application.port.out.IdempotencyPort;
import edu.dosw.styleradar.payments.application.port.out.MercadoPagoPort;
import edu.dosw.styleradar.payments.application.port.out.PaymentOrderRepositoryPort;
import edu.dosw.styleradar.payments.application.port.out.SubscriptionRepositoryPort;
import edu.dosw.styleradar.payments.application.port.out.WebhookLogPort;
import edu.dosw.styleradar.payments.application.service.SubscriptionActivationService;
import edu.dosw.styleradar.payments.domain.event.PaymentConfirmedEvent;
import edu.dosw.styleradar.payments.domain.exception.IdempotencyException;
import edu.dosw.styleradar.payments.domain.exception.PaymentProcessingException;
import edu.dosw.styleradar.payments.domain.exception.InvalidRequestException;
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
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WebhookUseCaseTest {

    @Mock
    private PaymentOrderRepositoryPort paymentOrderRepository;
    @Mock
    private SubscriptionRepositoryPort subscriptionRepository;
    @Mock
    private MercadoPagoPort mercadoPago;
    @Mock
    private IdempotencyPort idempotencyPort;
    @Mock
    private WebhookLogPort webhookLogPort;
    @Mock
    private SubscriptionActivationService activetionService;
    @Mock
    private EventPublisherPort eventPublisher;

    private WebhookUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new WebhookUseCase(paymentOrderRepository, subscriptionRepository, mercadoPago,
                idempotencyPort, webhookLogPort, activetionService, eventPublisher, TestFixtures.CLOCK);
    }

    private NotificationPayment notification(String id) {
        return new NotificationPayment(id, "payment", "req-1", Map.of("type", "payment"));
    }

    private MercadoPagoPort.MercadoPagoPayment payment(String status, String referencia, String preapproval) {
        return new MercadoPagoPort.MercadoPagoPayment("MP-1", status, referencia, preapproval,
                new BigDecimal("14900.00"), "COP");
    }

    private void lockLibre() {
        when(idempotencyPort.tryLock(anyString(), any(Duration.class))).thenReturn(true);
    }

    @Test
    void rechazaNotificationSinPayment() {
        assertThatThrownBy(() -> useCase.process(null, "corr"))
                .isInstanceOf(InvalidRequestException.class);
        assertThatThrownBy(() -> useCase.process(new NotificationPayment(" ", "payment", null, Map.of()), "corr"))
                .isInstanceOf(InvalidRequestException.class);
    }

    @Test
    void ignoraNotificationesDePreapproval() {
        useCase.process(new NotificationPayment("MP-1", "subscription_preapproval", null, Map.of()), "corr");

        verifyNoInteractions(mercadoPago, idempotencyPort, webhookLogPort);
    }

    @Test
    void resuelveAuthorizedPaymentRecurrenteYActivaSuscripcion() {
        lockLibre();
        when(mercadoPago.getAuthorizedPayment("INV-1")).thenReturn(
                new MercadoPagoPort.AuthorizedPayment("INV-1", "processed", "SUS-100",
                        "PRE-1", "MP-1", new BigDecimal("14900.00"), "COP"));
        when(webhookLogPort.exists("MP-1")).thenReturn(false);
        PaymentOrder pending = TestFixtures.conIdOrder(TestFixtures.pending(100L, "14900"), 10L);
        when(paymentOrderRepository.findByExternalPaymentId("MP-1")).thenReturn(Optional.of(pending));
        when(mercadoPago.getPayment("MP-1")).thenReturn(payment("approved", "SUS-100", "PRE-1"));
        when(paymentOrderRepository.save(any(PaymentOrder.class))).thenAnswer(inv -> inv.getArgument(0));
        Subscription activeda = TestFixtures.activeUser("USER-1",
                TestFixtures.HOY, TestFixtures.HOY.plusMonths(1));
        when(activetionService.activate(any(), any(), any())).thenReturn(activeda);
        when(subscriptionRepository.findById(100L)).thenReturn(Optional.of(activeda));

        useCase.process(new NotificationPayment("INV-1", "subscription_authorized_payment",
                "req-1", Map.of("type", "subscription_authorized_payment")), "corr");

        verify(mercadoPago).getAuthorizedPayment("INV-1");
        verify(mercadoPago).getPayment("MP-1");
        verify(activetionService).activate(any(), any(), any());
        verify(webhookLogPort).register(eq("MP-1"), anyMap(), eq("PROCESSED"));
        verify(idempotencyPort).release("lock:payment_MP-1");
    }

    @Test
    void respetaElBloqueoDeIdempotencia() {
        when(idempotencyPort.tryLock(eq("lock:payment_MP-1"), any(Duration.class)))
                .thenReturn(false);

        assertThatThrownBy(() -> useCase.process(notification("MP-1"), "corr"))
                .isInstanceOf(IdempotencyException.class);
        verify(mercadoPago, never()).getPayment(anyString());
    }

    @Test
    void omiteWebhookDuplicadoYLiberaElLock() {
        lockLibre();
        when(webhookLogPort.exists("MP-1")).thenReturn(true);

        useCase.process(notification("MP-1"), "corr");

        verify(mercadoPago, never()).getPayment(anyString());
        verify(idempotencyPort).release("lock:payment_MP-1");
    }

    @Test
    void omitePaymentNoAsociadoAStyleRadar() {
        lockLibre();
        when(webhookLogPort.exists("MP-1")).thenReturn(false);
        when(mercadoPago.getPayment("MP-1")).thenReturn(payment("approved", null, null));

        useCase.process(notification("MP-1"), "corr");

        verify(paymentOrderRepository, never()).save(any());
        verify(idempotencyPort).release("lock:payment_MP-1");
    }

    @Test
    void activeSubscriptionConPaymentApprovedDeUnaOrderExistente() {
        lockLibre();
        PaymentOrder pending = TestFixtures.conIdOrder(TestFixtures.pending(100L, "14900"), 10L);
        when(paymentOrderRepository.findByExternalPaymentId("MP-1")).thenReturn(Optional.of(pending));
        when(mercadoPago.getPayment("MP-1")).thenReturn(payment("approved", null, null));
        when(paymentOrderRepository.save(any(PaymentOrder.class))).thenAnswer(inv -> inv.getArgument(0));
        Subscription activeda = TestFixtures.activeUser("USER-1",
                TestFixtures.HOY, TestFixtures.HOY.plusMonths(1));
        when(activetionService.activate(any(), any(), any())).thenReturn(activeda);
        when(subscriptionRepository.findById(100L)).thenReturn(Optional.of(activeda));

        useCase.process(notification("MP-1"), "corr-webhook");

        verify(paymentOrderRepository).save(any(PaymentOrder.class));
        verify(activetionService).activate(any(), any(), any());
        verify(eventPublisher).publishPaymentConfirmed(any(PaymentConfirmedEvent.class));
        verify(webhookLogPort).register(eq("MP-1"), anyMap(), eq("PROCESSED"));
        verify(idempotencyPort).release("lock:payment_MP-1");
    }

    @Test
    void noRepiteActivationSiElStatusYaEraApproved() {
        lockLibre();
        PaymentOrder aprobada = TestFixtures.conIdOrder(TestFixtures.aprobada(100L, "14900"), 10L);
        when(paymentOrderRepository.findByExternalPaymentId("MP-1")).thenReturn(Optional.of(aprobada));
        when(mercadoPago.getPayment("MP-1")).thenReturn(payment("approved", null, null));

        useCase.process(notification("MP-1"), "corr");

        verify(paymentOrderRepository, never()).save(any());
        verify(activetionService, never()).activate(any(), any(), any());
        verify(eventPublisher, never()).publishPaymentConfirmed(any());
        verify(webhookLogPort).register(eq("MP-1"), anyMap(), eq("PROCESSED"));
    }

    @Test
    void actualizaPaymentRejectedSinActivete() {
        lockLibre();
        PaymentOrder pending = TestFixtures.conIdOrder(TestFixtures.pending(100L, "14900"), 10L);
        when(paymentOrderRepository.findByExternalPaymentId("MP-1")).thenReturn(Optional.of(pending));
        when(mercadoPago.getPayment("MP-1")).thenReturn(payment("rejected", null, null));
        when(paymentOrderRepository.save(any(PaymentOrder.class))).thenAnswer(inv -> inv.getArgument(0));

        useCase.process(notification("MP-1"), "corr");

        verify(paymentOrderRepository).save(any(PaymentOrder.class));
        verify(activetionService, never()).activate(any(), any(), any());
        verify(eventPublisher, never()).publishPaymentConfirmed(any());
    }

    @Test
    void ignoraStatusesDesconocidos() {
        lockLibre();
        PaymentOrder pending = TestFixtures.conIdOrder(TestFixtures.pending(100L, "14900"), 10L);
        when(paymentOrderRepository.findByExternalPaymentId("MP-1")).thenReturn(Optional.of(pending));
        when(mercadoPago.getPayment("MP-1")).thenReturn(payment("status-raro", null, null));

        useCase.process(notification("MP-1"), "corr");

        verify(paymentOrderRepository, never()).save(any());
        verify(idempotencyPort).release("lock:payment_MP-1");
    }

    @Test
    void asociaPorReferenciaExternaSUS() {
        lockLibre();
        Subscription subscription = TestFixtures.conIdSubscription(TestFixtures.pendingUser("USER-1"), 100L);
        PaymentOrder pending = TestFixtures.conIdOrder(TestFixtures.pending(100L, "14900"), 10L);
        when(paymentOrderRepository.findByExternalPaymentId("MP-1")).thenReturn(Optional.empty());
        when(mercadoPago.getPayment("MP-1")).thenReturn(payment("approved", "SUS-100", null));
        when(subscriptionRepository.findById(100L)).thenReturn(Optional.of(subscription));
        when(paymentOrderRepository.findPendingBySubscription(100L)).thenReturn(Optional.of(pending));
        when(paymentOrderRepository.save(any(PaymentOrder.class))).thenAnswer(inv -> inv.getArgument(0));
        when(activetionService.activate(any(), any(), any())).thenReturn(subscription);

        useCase.process(notification("MP-1"), "corr");

        verify(activetionService).activate(any(), any(), any());
        verify(eventPublisher).publishPaymentConfirmed(any());
    }

    @Test
    void asociaPorPreapprovalCuandoNoHayReferencia() {
        lockLibre();
        Subscription subscription = TestFixtures.conIdSubscription(TestFixtures.pendingUser("USER-1"), 100L);
        PaymentOrder pending = TestFixtures.conIdOrder(TestFixtures.pending(100L, "14900"), 10L);
        when(paymentOrderRepository.findByExternalPaymentId("MP-1")).thenReturn(Optional.empty());
        when(mercadoPago.getPayment("MP-1")).thenReturn(payment("approved", null, "PRE-100"));
        when(subscriptionRepository.findByPreapprovalId("PRE-100")).thenReturn(Optional.of(subscription));
        when(paymentOrderRepository.findPendingBySubscription(100L)).thenReturn(Optional.of(pending));
        when(paymentOrderRepository.save(any(PaymentOrder.class))).thenAnswer(inv -> inv.getArgument(0));
        when(activetionService.activate(any(), any(), any())).thenReturn(subscription);
        when(subscriptionRepository.findById(100L)).thenReturn(Optional.of(subscription));

        useCase.process(notification("MP-1"), "corr");

        verify(activetionService).activate(any(), any(), any());
    }

    @Test
    void creaOrderDeRenovacionSiNoExistePending() {
        lockLibre();
        Subscription subscription = TestFixtures.conIdSubscription(TestFixtures.activeUser("USER-1",
                TestFixtures.HOY.minusMonths(1), TestFixtures.HOY), 100L);
        when(paymentOrderRepository.findByExternalPaymentId("MP-1")).thenReturn(Optional.empty());
        when(mercadoPago.getPayment("MP-1")).thenReturn(payment("approved", "SUS-100", null));
        when(subscriptionRepository.findById(100L)).thenReturn(Optional.of(subscription));
        when(paymentOrderRepository.findPendingBySubscription(100L)).thenReturn(Optional.empty());
        when(paymentOrderRepository.save(any(PaymentOrder.class)))
                .thenAnswer(inv -> TestFixtures.conIdOrder(inv.getArgument(0), 77L));
        when(activetionService.activate(any(), any(), any())).thenReturn(subscription);

        useCase.process(notification("MP-1"), "corr");

        verify(paymentOrderRepository, org.mockito.Mockito.times(2)).save(any(PaymentOrder.class));
        verify(eventPublisher).publishPaymentConfirmed(any());
    }

    @Test
    void usaElAmountDeLaLatestOrderSiLaPasarelaNoLoInforma() {
        lockLibre();
        Subscription subscription = TestFixtures.conIdSubscription(TestFixtures.pendingUser("USER-1"), 100L);
        PaymentOrder anterior = TestFixtures.conIdOrder(TestFixtures.aprobada(100L, "89000.00"), 30L);
        when(paymentOrderRepository.findByExternalPaymentId("MP-1")).thenReturn(Optional.empty());
        when(mercadoPago.getPayment("MP-1")).thenReturn(new MercadoPagoPort.MercadoPagoPayment(
                "MP-1", "approved", "SUS-100", null, null, null));
        when(subscriptionRepository.findById(100L)).thenReturn(Optional.of(subscription));
        when(paymentOrderRepository.findPendingBySubscription(100L)).thenReturn(Optional.empty());
        when(paymentOrderRepository.findBySubscription(100L)).thenReturn(List.of(anterior));
        when(paymentOrderRepository.save(any(PaymentOrder.class)))
                .thenAnswer(inv -> TestFixtures.conIdOrder(inv.getArgument(0), 78L));
        when(activetionService.activate(any(), any(), any())).thenReturn(subscription);

        useCase.process(notification("MP-1"), "corr");

        verify(paymentOrderRepository, org.mockito.Mockito.times(2)).save(any(PaymentOrder.class));
    }

    @Test
    void fallaSiMercadoPagoNoRespondeYLiberaElLock() {
        lockLibre();
        when(webhookLogPort.exists("MP-1")).thenReturn(false);
        when(mercadoPago.getPayment("MP-1")).thenThrow(new IllegalStateException("caído"));

        assertThatThrownBy(() -> useCase.process(notification("MP-1"), "corr"))
                .isInstanceOf(PaymentProcessingException.class);
        verify(idempotencyPort).release("lock:payment_MP-1");
    }

    @Test
    void procesaNotificationSinTypeComoPayment() {
        lockLibre();
        when(webhookLogPort.exists("MP-1")).thenReturn(false);
        when(mercadoPago.getPayment("MP-1")).thenReturn(payment("pending", null, null));
        when(paymentOrderRepository.findByExternalPaymentId("MP-1")).thenReturn(Optional.of(
                TestFixtures.conIdOrder(TestFixtures.pending(100L, "14900"), 10L)));

        useCase.process(new NotificationPayment("MP-1", null, null, Map.of()), "corr");

        verify(webhookLogPort).register(eq("MP-1"), anyMap(), eq("PROCESSED"));
    }
}
