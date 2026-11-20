package edu.dosw.styleradar.payments.application.usecase;

import edu.dosw.styleradar.payments.application.dto.request.NotificationPayment;
import edu.dosw.styleradar.payments.application.port.in.WebhookPort;
import edu.dosw.styleradar.payments.application.port.out.EventPublisherPort;
import edu.dosw.styleradar.payments.application.port.out.IdempotencyPort;
import edu.dosw.styleradar.payments.application.port.out.MercadoPagoPort;
import edu.dosw.styleradar.payments.application.port.out.PaymentOrderRepositoryPort;
import edu.dosw.styleradar.payments.application.port.out.SubscriptionRepositoryPort;
import edu.dosw.styleradar.payments.application.port.out.WebhookLogPort;
import edu.dosw.styleradar.payments.application.service.PaymentStatusMapper;
import edu.dosw.styleradar.payments.application.service.SubscriptionActivationService;
import edu.dosw.styleradar.payments.domain.event.PaymentConfirmedEvent;
import edu.dosw.styleradar.payments.domain.exception.IdempotencyException;
import edu.dosw.styleradar.payments.domain.exception.InvalidRequestException;
import edu.dosw.styleradar.payments.domain.exception.PaymentProcessingException;
import edu.dosw.styleradar.payments.domain.exception.SubscriptionNotFoundException;
import edu.dosw.styleradar.payments.domain.model.Amount;
import edu.dosw.styleradar.payments.domain.model.PaymentOrder;
import edu.dosw.styleradar.payments.domain.model.PaymentStatus;
import edu.dosw.styleradar.payments.domain.model.Subscription;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
public class WebhookUseCase implements WebhookPort {

    private static final Duration LOCK_TTL = Duration.ofSeconds(30);
    private static final String REFERENCE_PREFIX = "SUS-";

    private final PaymentOrderRepositoryPort paymentOrderRepository;
    private final SubscriptionRepositoryPort subscriptionRepository;
    private final MercadoPagoPort mercadoPago;
    private final IdempotencyPort idempotencyPort;
    private final WebhookLogPort webhookLogPort;
    private final SubscriptionActivationService activationService;
    private final EventPublisherPort eventPublisher;
    private final Clock clock;

    public WebhookUseCase(PaymentOrderRepositoryPort paymentOrderRepository,
                          SubscriptionRepositoryPort subscriptionRepository,
                          MercadoPagoPort mercadoPago,
                          IdempotencyPort idempotencyPort,
                          WebhookLogPort webhookLogPort,
                          SubscriptionActivationService activationService,
                          EventPublisherPort eventPublisher,
                          Clock clock) {
        this.paymentOrderRepository = paymentOrderRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.mercadoPago = mercadoPago;
        this.idempotencyPort = idempotencyPort;
        this.webhookLogPort = webhookLogPort;
        this.activationService = activationService;
        this.eventPublisher = eventPublisher;
        this.clock = clock;
    }

    @Override
    @Transactional
    public void process(NotificationPayment notification, String correlationId) {
        if (notification == null || notification.paymentId() == null || notification.paymentId().isBlank()) {
            throw new InvalidRequestException("La notificación no contiene el identificador del pago.");
        }
        if (notification.type() != null && !isPaymentNotification(notification.type())) {
            log.info("Notification ignored for type {} (payment {})", notification.type(), notification.paymentId());
            return;
        }

        String key = "lock:payment_" + notification.paymentId();
        if (!idempotencyPort.tryLock(key, LOCK_TTL)) {
            throw new IdempotencyException();
        }
        try {
            processWithLock(notification, correlationId);
        } finally {
            idempotencyPort.release(key);
        }
    }

    private void processWithLock(NotificationPayment notification, String correlationId) {
        if (webhookLogPort.exists(notification.paymentId())) {
            log.info("Duplicate webhook for payment {}; skipped", notification.paymentId());
            return;
        }

        MercadoPagoPort.MercadoPagoPayment payment = queryPayment(notification.paymentId());
        PaymentOrder order = findOrder(notification.paymentId(), payment);
        if (order == null) {
            log.warn("Could not associate payment {} with a StyleRadar subscription", notification.paymentId());
            return;
        }

        PaymentStatus newStatus = PaymentStatusMapper.fromMercadoPago(payment.status()).orElse(null);
        if (newStatus == null) {
            log.warn("Unrecognized Mercado Pago status: {} for payment {}",
                    payment.status(), notification.paymentId());
            return;
        }

        boolean transition = order.getStatus() != newStatus;
        if (transition) {
            order.updateStatus(newStatus, notification.paymentId());
            order = paymentOrderRepository.save(order);
        }

        if (newStatus == PaymentStatus.APPROVED && transition) {
            activateSubscription(order, correlationId);
        }

        Map<String, Object> payload = notification.payload() == null ? Map.of() : notification.payload();
        webhookLogPort.register(notification.paymentId(), payload, "PROCESSED");
    }

    private MercadoPagoPort.MercadoPagoPayment queryPayment(String paymentId) {
        try {
            MercadoPagoPort.MercadoPagoPayment payment = mercadoPago.getPayment(paymentId);
            if (payment == null || payment.id() == null || payment.id().isBlank()) {
                throw new PaymentProcessingException(
                        "No fue posible consultar el estado del pago en Mercado Pago.");
            }
            return payment;
        } catch (PaymentProcessingException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            throw new PaymentProcessingException(
                    "No fue posible consultar el estado del pago en Mercado Pago.", ex);
        }
    }

    private PaymentOrder findOrder(String paymentId, MercadoPagoPort.MercadoPagoPayment payment) {
        Optional<PaymentOrder> byExternalId = paymentOrderRepository.findByExternalPaymentId(
                payment.id() == null ? paymentId : payment.id());
        if (byExternalId.isPresent()) {
            return byExternalId.get();
        }

        Subscription subscription = findSubscription(payment);
        if (subscription == null || subscription.getId() == null) {
            return null;
        }

        Optional<PaymentOrder> pending = paymentOrderRepository.findPendingBySubscription(subscription.getId());
        if (pending.isPresent()) {
            return pending.get();
        }
        Amount amount = paymentAmount(payment, subscription.getId());
        return paymentOrderRepository.save(PaymentOrder.pending(
                subscription.getId(), amount, LocalDateTime.now(clock)));
    }

    private Subscription findSubscription(MercadoPagoPort.MercadoPagoPayment payment) {
        Long subscriptionId = parseSubscription(payment.externalReference());
        if (subscriptionId != null) {
            Optional<Subscription> byReference = subscriptionRepository.findById(subscriptionId);
            if (byReference.isPresent()) {
                return byReference.get();
            }
        }
        if (payment.preapprovalId() != null && !payment.preapprovalId().isBlank()) {
            return subscriptionRepository.findByPreapprovalId(payment.preapprovalId()).orElse(null);
        }
        return null;
    }

    private Amount paymentAmount(MercadoPagoPort.MercadoPagoPayment payment, Long subscriptionId) {
        if (payment.amount() != null) {
            String currency = payment.currency() == null || payment.currency().isBlank() ? "COP" : payment.currency();
            return new Amount(payment.amount(), currency);
        }
        return paymentOrderRepository.findBySubscription(subscriptionId).stream()
                .findFirst()
                .map(PaymentOrder::getAmount)
                .orElseThrow(() -> new PaymentProcessingException(
                        "No fue posible determinar el monto del pago recurrente."));
    }

    private Long parseSubscription(String externalReference) {
        if (externalReference == null || !externalReference.startsWith(REFERENCE_PREFIX)) {
            return null;
        }
        String[] parts = externalReference.split("-");
        if (parts.length < 2) {
            return null;
        }
        try {
            return Long.valueOf(parts[1]);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private void activateSubscription(PaymentOrder order, String correlationId) {
        Subscription subscription = subscriptionRepository.findById(order.getSubscriptionId())
                .orElseThrow(SubscriptionNotFoundException::new);
        Subscription activated = activationService.activate(subscription, order, LocalDate.now(clock));
        eventPublisher.publishPaymentConfirmed(PaymentConfirmedEvent.create(
                new PaymentConfirmedEvent.Data(
                        order.getId(),
                        activated.getId(),
                        activated.getSubscriberId(),
                        PaymentConfirmedEvent.userIdFrom(activated.getSubscriberId()),
                        activated.getSubscriberType().legacyValue(),
                        activated.getPlanId(),
                        activated.getTaxId(),
                        activated.getStoreId(),
                        activated.getStatus().legacyValue()),
                correlationId));
    }

    private boolean isPaymentNotification(String type) {
        return "payment".equalsIgnoreCase(type);
    }
}
