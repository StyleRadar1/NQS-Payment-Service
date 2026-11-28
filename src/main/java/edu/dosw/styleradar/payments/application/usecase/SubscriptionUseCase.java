package edu.dosw.styleradar.payments.application.usecase;

import edu.dosw.styleradar.payments.application.dto.response.StoresUpToDateResponse;
import edu.dosw.styleradar.payments.application.dto.request.StartSubscriptionRequest;
import edu.dosw.styleradar.payments.application.dto.response.CancellationResponse;
import edu.dosw.styleradar.payments.application.dto.response.StartSubscriptionResponse;
import edu.dosw.styleradar.payments.application.dto.response.PlanResponse;
import edu.dosw.styleradar.payments.application.dto.response.SubscriptionResponse;
import edu.dosw.styleradar.payments.application.port.in.SubscriptionPort;
import edu.dosw.styleradar.payments.application.port.out.MercadoPagoPort;
import edu.dosw.styleradar.payments.application.port.out.PaymentOrderRepositoryPort;
import edu.dosw.styleradar.payments.application.port.out.PlanRepositoryPort;
import edu.dosw.styleradar.payments.application.port.out.SubscriptionRepositoryPort;
import edu.dosw.styleradar.payments.application.service.Roles;
import edu.dosw.styleradar.payments.application.service.EmailValidator;
import edu.dosw.styleradar.payments.domain.exception.Messages;
import edu.dosw.styleradar.payments.domain.exception.UnauthorizedException;
import edu.dosw.styleradar.payments.domain.exception.ConflictException;
import edu.dosw.styleradar.payments.domain.exception.OperationNotAllowedException;
import edu.dosw.styleradar.payments.domain.exception.PaymentProcessingException;
import edu.dosw.styleradar.payments.domain.exception.PlanNotFoundException;
import edu.dosw.styleradar.payments.domain.exception.InvalidRequestException;
import edu.dosw.styleradar.payments.domain.exception.SubscriptionNotFoundException;
import edu.dosw.styleradar.payments.domain.model.PaymentOrder;
import edu.dosw.styleradar.payments.domain.model.Plan;
import edu.dosw.styleradar.payments.domain.model.Subscription;
import edu.dosw.styleradar.payments.domain.model.SubscriberType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class SubscriptionUseCase implements SubscriptionPort {

    private final PlanRepositoryPort planRepository;
    private final SubscriptionRepositoryPort subscriptionRepository;
    private final PaymentOrderRepositoryPort paymentOrderRepository;
    private final MercadoPagoPort mercadoPago;
    private final Clock clock;
    private final String backUrl;

    public SubscriptionUseCase(PlanRepositoryPort planRepository,
                              SubscriptionRepositoryPort subscriptionRepository,
                              PaymentOrderRepositoryPort paymentOrderRepository,
                              MercadoPagoPort mercadoPago,
                              Clock clock,
                              @Value("${mercadopago.back-url}") String backUrl) {
        this.planRepository = planRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.paymentOrderRepository = paymentOrderRepository;
        this.mercadoPago = mercadoPago;
        this.clock = clock;
        this.backUrl = backUrl;
    }

    @Override
    @Transactional(readOnly = true)
    public StoresUpToDateResponse listStoresUpToDate() {
        return new StoresUpToDateResponse(subscriptionRepository.findActiveStores(LocalDate.now(clock)).stream()
                .map(Subscription::getTaxId)
                .filter(taxId -> taxId != null && !taxId.isBlank())
                .distinct()
                .sorted()
                .toList());
    }

    @Override
    @Transactional
    public StartSubscriptionResponse start(String subscriberId, StartSubscriptionRequest request) {
        if (subscriberId == null || subscriberId.isBlank()) {
            throw new UnauthorizedException(Messages.SESSION_REQUIRED);
        }
        if (request == null || request.subscriberType() == null) {
            throw new InvalidRequestException(Messages.SUBSCRIBER_TYPE_REQUIRED);
        }
        SubscriberType type = request.subscriberType();
        validateEmail(request.payerEmail(), type);

        Plan plan = resolvePlan(request);
        Optional<Subscription> current = subscriptionRepository.findActiveOrPending(subscriberId);
        if (current.isPresent()) {
            throw new ConflictException("SUBSCRIPTION_ALREADY_EXISTS", type == SubscriberType.STORE
                    ? Messages.STORE_ALREADY_SUBSCRIBED
                    : Messages.RADAR_PLUS_ACTIVE);
        }

        Subscription subscription = subscriptionRepository.findLatestBySubscriber(subscriberId)
                .filter(s -> s.isSuspended()
                        && s.isOfType(type)
                        && s.getPlanId().equals(plan.getId()))
                .orElseGet(() -> Subscription.pending(subscriberId, type, plan.getId(), LocalDateTime.now(clock)));

        if (type == SubscriberType.STORE) {
            if (request.taxId() == null || !request.taxId().matches("\\d{9}(-\\d)?")) {
                throw new InvalidRequestException(Messages.TAX_ID_REQUIRED);
            }
            subscription.assignTaxId(request.taxId());
            // El NIT es el identificador canonico de tienda. storeId se conserva
            // como dato legado opcional para no romper integraciones anteriores.
            if (request.storeId() != null && !request.storeId().isBlank()) {
                try {
                    subscription.assignStoreId(java.util.UUID.fromString(request.storeId().trim()));
                } catch (IllegalArgumentException ignored) {
                    // Un storeId legado (por ejemplo el id de Auth o el NIT) no es UUID.
                    // La tienda queda identificada por taxId.
                }
            }
        }

        subscription = subscriptionRepository.save(subscription);

        MercadoPagoPort.PreapprovalCreated preapproval;
        try {
            preapproval = mercadoPago.createPreapproval(new MercadoPagoPort.PreapprovalRequest(
                    "Subscription " + plan.getName() + " - StyleRadar",
                    "SUS-" + subscription.getId(),
                    request.payerEmail().trim(),
                    plan.getPrice(),
                    backUrl));
        } catch (RuntimeException ex) {
            throw new PaymentProcessingException(Messages.PAYMENT_START_FAILED, ex);
        }
        if (preapproval == null || preapproval.id() == null || preapproval.id().isBlank()
                || preapproval.initPoint() == null || preapproval.initPoint().isBlank()) {
            throw new PaymentProcessingException(Messages.PAYMENT_START_FAILED);
        }

        subscription.assignPreapproval(preapproval.id());
        subscription = subscriptionRepository.save(subscription);

        PaymentOrder order = paymentOrderRepository.save(PaymentOrder.pending(
                subscription.getId(), plan.getPrice(), LocalDateTime.now(clock)));

        String message = type == SubscriberType.STORE
                ? "Continúa a Mercado Pago para activar tu almacén."
                : "Continúa a Mercado Pago para activar Radar+.";
        return new StartSubscriptionResponse(
                subscription.getId(), order.getId(), subscription.getStatus(), preapproval.initPoint(), message);
    }

    @Override
    @Transactional(readOnly = true)
    public SubscriptionResponse get(String subscriberId, String requesterId, String role) {
        Subscription subscription = subscriptionRepository.findLatestBySubscriber(subscriberId)
                .orElseThrow(SubscriptionNotFoundException::new);
        validateAccess(subscription, requesterId, role);

        Plan plan = planRepository.findById(subscription.getPlanId())
                .orElseThrow(PlanNotFoundException::new);
        LocalDate today = LocalDate.now(clock);
        boolean access = subscription.allowsAccess(today);
        String message = messageStatus(subscription);
        return SubscriptionResponse.from(subscription, PlanResponse.from(plan), access, message);
    }

    @Override
    @Transactional
    public CancellationResponse cancel(String subscriberId,
                                        boolean confirmed,
                                        String requesterId,
                                        String role) {
        if (!confirmed) {
            throw new InvalidRequestException(Messages.CONFIRM_CANCELLATION_REQUIRED);
        }
        Subscription subscription = subscriptionRepository.findLatestBySubscriber(subscriberId)
                .orElseThrow(SubscriptionNotFoundException::new);
        validateAccess(subscription, requesterId, role);

        if (!subscription.isActive()) {
            throw new InvalidRequestException(Messages.SUBSCRIPTION_NOT_ACTIVE);
        }
        if (!subscription.isAutoRenewal()) {
            return new CancellationResponse(
                    subscription.getId(), false, subscription.getStatus(), subscription.getEndDate(),
                    true, "La renovación automática ya estaba desactivada.");
        }

        try {
            if (subscription.getMercadoPagoPreapprovalId() != null
                    && !subscription.getMercadoPagoPreapprovalId().isBlank()) {
                mercadoPago.cancelPreapproval(subscription.getMercadoPagoPreapprovalId());
            }
        } catch (RuntimeException ex) {
            String message = subscription.getSubscriberType() == SubscriberType.USER
                    ? Messages.RADAR_PLUS_RENEWAL_CANCEL_FAILED
                    : Messages.RENEWAL_CANCEL_FAILED;
            throw new PaymentProcessingException(message, ex);
        }

        subscription.cancelRenewal();
        subscription = subscriptionRepository.save(subscription);

        String message = subscription.getSubscriberType() == SubscriberType.USER
                ? "La renovación de Radar+ fue cancelada. Mantienes los beneficios hasta "
                    + subscription.getEndDate() + "."
                : "La renovación de tu suscripción fue cancelada. Mantienes el acceso hasta "
                    + subscription.getEndDate() + ".";
        return new CancellationResponse(
                subscription.getId(), false, subscription.getStatus(), subscription.getEndDate(), true, message);
    }

    private Plan resolvePlan(StartSubscriptionRequest request) {
        Plan plan = planRepository.findPaidPlanByType(request.subscriberType())
                .orElseThrow(PlanNotFoundException::new);
        if (request.planId() != null && !plan.getId().equals(request.planId())) {
            if (request.subscriberType() == SubscriberType.USER) {
                throw new PlanNotFoundException();
            }
        }
        return plan;
    }

    private void validateEmail(String email, SubscriberType type) {
        if (!EmailValidator.isValid(email)) {
            throw new InvalidRequestException(type == SubscriberType.USER
                    ? Messages.INVALID_EMAIL_RADAR_PLUS
                    : Messages.INVALID_EMAIL);
        }
    }

    private void validateAccess(Subscription subscription, String requesterId, String role) {
        if (requesterId == null || requesterId.isBlank()) {
            throw new UnauthorizedException(Messages.SESSION_REQUIRED);
        }
        if (!Roles.isAdmin(role) && !Roles.isOwner(subscription.getSubscriberId(), requesterId)) {
            throw new OperationNotAllowedException(Messages.OWNER_ONLY);
        }
    }

    private String messageStatus(Subscription subscription) {
        if (subscription.isSuspended() && subscription.getSubscriberType() == SubscriberType.USER) {
            return Messages.RADAR_PLUS_ENDED;
        }
        if (subscription.isPending()) {
            return Messages.PAYMENT_PENDING;
        }
        return null;
    }
}

