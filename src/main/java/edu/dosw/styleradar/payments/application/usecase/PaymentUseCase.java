package edu.dosw.styleradar.payments.application.usecase;

import edu.dosw.styleradar.payments.application.dto.response.PaymentResponse;
import edu.dosw.styleradar.payments.application.dto.response.RetryPaymentResponse;
import edu.dosw.styleradar.payments.application.port.in.PaymentPort;
import edu.dosw.styleradar.payments.application.port.out.MercadoPagoPort;
import edu.dosw.styleradar.payments.application.port.out.PaymentOrderRepositoryPort;
import edu.dosw.styleradar.payments.application.port.out.SubscriptionRepositoryPort;
import edu.dosw.styleradar.payments.application.service.EmailValidator;
import edu.dosw.styleradar.payments.application.service.Roles;
import edu.dosw.styleradar.payments.domain.exception.InvalidRequestException;
import edu.dosw.styleradar.payments.domain.exception.Messages;
import edu.dosw.styleradar.payments.domain.exception.OperationNotAllowedException;
import edu.dosw.styleradar.payments.domain.exception.PaymentOrderNotFoundException;
import edu.dosw.styleradar.payments.domain.exception.PaymentProcessingException;
import edu.dosw.styleradar.payments.domain.exception.SubscriptionNotFoundException;
import edu.dosw.styleradar.payments.domain.exception.UnauthorizedException;
import edu.dosw.styleradar.payments.domain.model.PaymentOrder;
import edu.dosw.styleradar.payments.domain.model.PaymentStatus;
import edu.dosw.styleradar.payments.domain.model.Subscription;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class PaymentUseCase implements PaymentPort {

    private final PaymentOrderRepositoryPort paymentOrderRepository;
    private final SubscriptionRepositoryPort subscriptionRepository;
    private final MercadoPagoPort mercadoPago;
    private final Clock clock;
    private final String backUrl;

    public PaymentUseCase(PaymentOrderRepositoryPort paymentOrderRepository,
                          SubscriptionRepositoryPort subscriptionRepository,
                          MercadoPagoPort mercadoPago,
                          Clock clock,
                          @Value("${mercadopago.back-url}") String backUrl) {
        this.paymentOrderRepository = paymentOrderRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.mercadoPago = mercadoPago;
        this.clock = clock;
        this.backUrl = backUrl;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> history(String subscriberId) {
        if (subscriberId == null || subscriberId.isBlank()) {
            throw new UnauthorizedException(Messages.SESSION_REQUIRED);
        }
        return paymentOrderRepository.findBySubscriber(subscriberId).stream()
                .map(PaymentResponse::from)
                .toList();
    }

    @Override
    @Transactional
    public RetryPaymentResponse retry(Long orderId, String payerEmail, String requesterId) {
        PaymentOrder original = paymentOrderRepository.findById(orderId)
                .orElseThrow(PaymentOrderNotFoundException::new);
        Subscription subscription = subscriptionRepository.findById(original.getSubscriptionId())
                .orElseThrow(SubscriptionNotFoundException::new);

        if (requesterId == null || requesterId.isBlank()) {
            throw new UnauthorizedException(Messages.SESSION_REQUIRED);
        }
        if (!Roles.isOwner(subscription.getSubscriberId(), requesterId)) {
            throw new OperationNotAllowedException(Messages.OWNER_ONLY);
        }
        if (subscription.isActive()) {
            throw new InvalidRequestException(Messages.SUBSCRIPTION_ACTIVE_NO_RETRY);
        }
        if (!original.isRejected()) {
            throw new InvalidRequestException(Messages.ORDER_NOT_REJECTED);
        }
        if (!EmailValidator.isValid(payerEmail)) {
            throw new InvalidRequestException(Messages.INVALID_EMAIL);
        }

        PaymentOrder newOrder = paymentOrderRepository.save(PaymentOrder.pending(
                subscription.getId(), original.getAmount(), LocalDateTime.now(clock)));

        MercadoPagoPort.PreapprovalCreated preapproval;
        try {
            preapproval = mercadoPago.createPreapproval(new MercadoPagoPort.PreapprovalRequest(
                    "Reintento de pago - StyleRadar",
                    "SUS-" + subscription.getId(),
                    payerEmail.trim(),
                    original.getAmount(),
                    backUrl));
        } catch (RuntimeException ex) {
            throw new PaymentProcessingException(Messages.RETRY_FAILED, ex);
        }
        if (preapproval == null || preapproval.initPoint() == null || preapproval.initPoint().isBlank()) {
            throw new PaymentProcessingException(Messages.RETRY_FAILED);
        }

        String previousPreapproval = subscription.getMercadoPagoPreapprovalId();
        subscription.assignPreapproval(preapproval.id());
        subscriptionRepository.save(subscription);
        cancelPreviousPreapproval(previousPreapproval, preapproval.id());

        return new RetryPaymentResponse(
                newOrder.getId(),
                PaymentStatus.PENDING,
                preapproval.initPoint(),
                "Nuevo intento de pago generado. Continúa en Mercado Pago.");
    }

    private void cancelPreviousPreapproval(String previousPreapproval, String newPreapproval) {
        if (previousPreapproval == null || previousPreapproval.isBlank()
                || previousPreapproval.equals(newPreapproval)) {
            return;
        }
        try {
            mercadoPago.cancelPreapproval(previousPreapproval);
        } catch (RuntimeException ex) {
            log.warn("Could not cancel previous preapproval {}: {}", previousPreapproval, ex.getMessage());
        }
    }
}
