package edu.dosw.styleradar.payments.application.service;

import edu.dosw.styleradar.payments.application.port.out.EventPublisherPort;
import edu.dosw.styleradar.payments.application.port.out.MercadoPagoPort;
import edu.dosw.styleradar.payments.application.port.out.PaymentOrderRepositoryPort;
import edu.dosw.styleradar.payments.application.port.out.SubscriptionRepositoryPort;
import edu.dosw.styleradar.payments.domain.event.PaymentConfirmedEvent;
import edu.dosw.styleradar.payments.domain.model.PaymentStatus;
import edu.dosw.styleradar.payments.domain.model.PaymentOrder;
import edu.dosw.styleradar.payments.domain.model.Subscription;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "styleradar.scheduling", name = "enabled",
        havingValue = "true", matchIfMissing = true)
public class ReconciliationScheduler {

    private static final long HORAS_ESPERA = 2L;

    private final PaymentOrderRepositoryPort paymentOrderRepository;
    private final SubscriptionRepositoryPort subscriptionRepository;
    private final MercadoPagoPort mercadoPago;
    private final SubscriptionActivationService activetionService;
    private final EventPublisherPort eventPublisher;
    private final Clock clock;

    public ReconciliationScheduler(PaymentOrderRepositoryPort paymentOrderRepository,
                                 SubscriptionRepositoryPort subscriptionRepository,
                                 MercadoPagoPort mercadoPago,
                                 SubscriptionActivationService activetionService,
                                 EventPublisherPort eventPublisher,
                                 Clock clock) {
        this.paymentOrderRepository = paymentOrderRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.mercadoPago = mercadoPago;
        this.activetionService = activetionService;
        this.eventPublisher = eventPublisher;
        this.clock = clock;
    }

    @Scheduled(cron = "${styleradar.scheduling.reconciliation-cron:0 0 3 * * *}")
    public void conciliarPaymentsPendings() {
        LocalDateTime limit = LocalDateTime.now(clock).minusHours(HORAS_ESPERA);
        List<PaymentOrder> pendingOrders = paymentOrderRepository.findPendingBefore(limit);
        int corregidas = 0;
        for (PaymentOrder order : pendingOrders) {
            if (order.getExternalPaymentId() == null || order.getExternalPaymentId().isBlank()) {
                continue;
            }
            try {
                MercadoPagoPort.MercadoPagoPayment payment = mercadoPago.getPayment(order.getExternalPaymentId());
                if (payment == null) {
                    continue;
                }
                PaymentStatus actualStatus = PaymentStatusMapper.fromMercadoPago(payment.status()).orElse(null);
                if (actualStatus == null || actualStatus == order.getStatus()) {
                    continue;
                }
                order.updateStatus(actualStatus, payment.id());
                PaymentOrder guardada = paymentOrderRepository.save(order);
                if (actualStatus == PaymentStatus.APPROVED) {
                    activateYPublicar(guardada);
                }
                corregidas++;
            } catch (RuntimeException ex) {
                log.error("Could not reconcile order {}: {}", order.getId(), ex.getMessage());
            }
        }
        log.info("Daily reconciliation: {} pending orders reviewed, {} corrected",
                pendingOrders.size(), corregidas);
    }

    private void activateYPublicar(PaymentOrder order) {
        Subscription subscription = subscriptionRepository.findById(order.getSubscriptionId())
                .orElseThrow(() -> new IllegalStateException(
                        "Subscription not found for order " + order.getId()));
        Subscription activeda = activetionService.activate(subscription, order, LocalDate.now(clock));
        eventPublisher.publishPaymentConfirmed(PaymentConfirmedEvent.create(
                new PaymentConfirmedEvent.Data(
                        order.getId(),
                        activeda.getId(),
                        activeda.getSubscriberId(),
                        PaymentConfirmedEvent.userIdFrom(activeda.getSubscriberId()),
                        activeda.getSubscriberType().legacyValue(),
                        activeda.getPlanId(),
                        activeda.getTaxId(),
                        activeda.getStoreId(),
                        activeda.getStatus().legacyValue()),
                null));
    }
}
