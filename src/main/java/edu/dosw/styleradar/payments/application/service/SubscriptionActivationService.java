package edu.dosw.styleradar.payments.application.service;

import edu.dosw.styleradar.payments.application.port.out.SubscriptionRepositoryPort;
import edu.dosw.styleradar.payments.domain.model.PaymentOrder;
import edu.dosw.styleradar.payments.domain.model.Subscription;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Objects;

@Service
public class SubscriptionActivationService {

    private final SubscriptionRepositoryPort subscriptionRepository;

    public SubscriptionActivationService(SubscriptionRepositoryPort subscriptionRepository) {
        this.subscriptionRepository = Objects.requireNonNull(subscriptionRepository);
    }

    public Subscription activate(Subscription subscription, PaymentOrder order, LocalDate datePayment) {
        Objects.requireNonNull(subscription, "La suscripción es obligatoria.");
        Objects.requireNonNull(order, "La orden es obligatoria.");
        if (!order.isApproved()) {
            throw new IllegalStateException("Solo un pago aprobado activa o extiende una suscripción.");
        }
        if (subscription.getId() != null && !subscription.getId().equals(order.getSubscriptionId())) {
            throw new IllegalStateException("La orden no pertenece a la suscripción.");
        }
        subscription.activate(datePayment);
        return subscriptionRepository.save(subscription);
    }
}

