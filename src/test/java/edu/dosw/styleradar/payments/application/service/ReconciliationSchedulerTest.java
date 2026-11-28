package edu.dosw.styleradar.payments.application.service;

import edu.dosw.styleradar.payments.TestFixtures;
import edu.dosw.styleradar.payments.application.port.out.EventPublisherPort;
import edu.dosw.styleradar.payments.application.port.out.MercadoPagoPort;
import edu.dosw.styleradar.payments.application.port.out.PaymentOrderRepositoryPort;
import edu.dosw.styleradar.payments.application.port.out.SubscriptionRepositoryPort;
import edu.dosw.styleradar.payments.domain.event.PaymentConfirmedEvent;
import edu.dosw.styleradar.payments.domain.model.PaymentStatus;
import edu.dosw.styleradar.payments.domain.model.PaymentOrder;
import edu.dosw.styleradar.payments.domain.model.Subscription;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReconciliationSchedulerTest {

    @Mock
    private PaymentOrderRepositoryPort paymentOrderRepository;
    @Mock
    private SubscriptionRepositoryPort subscriptionRepository;
    @Mock
    private MercadoPagoPort mercadoPago;
    @Mock
    private SubscriptionActivationService activetionService;
    @Mock
    private EventPublisherPort eventPublisher;

    private ReconciliationScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new ReconciliationScheduler(paymentOrderRepository, subscriptionRepository,
                mercadoPago, activetionService, eventPublisher, TestFixtures.CLOCK);
    }

    private PaymentOrder pendingConPayment() {
        PaymentOrder order = TestFixtures.conIdOrder(TestFixtures.pending(100L, "14900"), 10L);
        order.updateStatus(PaymentStatus.PENDING, "MP-1");
        return order;
    }

    @Test
    void corrigeStatusApprovedYActive() {
        PaymentOrder order = pendingConPayment();
        Subscription subscription = TestFixtures.activeUser("USER-1",
                TestFixtures.HOY, TestFixtures.HOY.plusMonths(1));
        when(paymentOrderRepository.findPendingBefore(any())).thenReturn(List.of(order));
        when(mercadoPago.getPayment("MP-1")).thenReturn(new MercadoPagoPort.MercadoPagoPayment(
                "MP-1", "approved", null, null, new BigDecimal("14900.00"), "COP"));
        when(paymentOrderRepository.save(any(PaymentOrder.class))).thenAnswer(inv -> inv.getArgument(0));
        when(subscriptionRepository.findById(100L)).thenReturn(Optional.of(subscription));
        when(activetionService.activate(any(), any(), any())).thenReturn(subscription);

        scheduler.conciliarPaymentsPendings();

        assertThat(order.getStatus()).isEqualTo(PaymentStatus.APPROVED);
        verify(activetionService).activate(any(), any(), any());
        verify(eventPublisher).publishPaymentConfirmed(any(PaymentConfirmedEvent.class));
    }

    @Test
    void omiteOrderSinPaymentExterno() {
        PaymentOrder sinPayment = TestFixtures.conIdOrder(TestFixtures.pending(100L, "14900"), 11L);
        when(paymentOrderRepository.findPendingBefore(any())).thenReturn(List.of(sinPayment));

        scheduler.conciliarPaymentsPendings();

        verify(mercadoPago, never()).getPayment(anyString());
    }

    @Test
    void omiteSiElStatusNoCambioOEsDesconocido() {
        PaymentOrder order = pendingConPayment();
        when(paymentOrderRepository.findPendingBefore(any())).thenReturn(List.of(order));
        when(mercadoPago.getPayment("MP-1"))
                .thenReturn(new MercadoPagoPort.MercadoPagoPayment("MP-1", "pending", null, null, null, null))
                .thenReturn(new MercadoPagoPort.MercadoPagoPayment("MP-1", "raro", null, null, null, null));

        scheduler.conciliarPaymentsPendings();
        scheduler.conciliarPaymentsPendings();

        verify(paymentOrderRepository, never()).save(any());
    }

    @Test
    void omiteSiMercadoPagoNoDevuelveElPayment() {
        when(paymentOrderRepository.findPendingBefore(any())).thenReturn(List.of(pendingConPayment()));
        when(mercadoPago.getPayment("MP-1")).thenReturn(null);

        scheduler.conciliarPaymentsPendings();

        verify(paymentOrderRepository, never()).save(any());
    }

    @Test
    void continuaConLasDemasSiUnaFalla() {
        PaymentOrder primera = pendingConPayment();
        PaymentOrder segunda = TestFixtures.conIdOrder(TestFixtures.pending(101L, "14900"), 20L);
        segunda.updateStatus(PaymentStatus.PENDING, "MP-2");
        when(paymentOrderRepository.findPendingBefore(any())).thenReturn(List.of(primera, segunda));
        when(mercadoPago.getPayment("MP-1")).thenThrow(new IllegalStateException("MP"));
        when(mercadoPago.getPayment("MP-2")).thenReturn(new MercadoPagoPort.MercadoPagoPayment(
                "MP-2", "rejected", null, null, null, null));
        when(paymentOrderRepository.save(any(PaymentOrder.class))).thenAnswer(inv -> inv.getArgument(0));

        scheduler.conciliarPaymentsPendings();

        assertThat(segunda.getStatus()).isEqualTo(PaymentStatus.REJECTED);
    }

    @Test
    void capturaFalloAlActivete() {
        PaymentOrder order = pendingConPayment();
        when(paymentOrderRepository.findPendingBefore(any())).thenReturn(List.of(order));
        when(mercadoPago.getPayment("MP-1")).thenReturn(new MercadoPagoPort.MercadoPagoPayment(
                "MP-1", "approved", null, null, null, null));
        when(paymentOrderRepository.save(any(PaymentOrder.class))).thenAnswer(inv -> inv.getArgument(0));
        when(subscriptionRepository.findById(100L)).thenReturn(Optional.empty());

        scheduler.conciliarPaymentsPendings();

        verify(eventPublisher, never()).publishPaymentConfirmed(any());
    }
}
