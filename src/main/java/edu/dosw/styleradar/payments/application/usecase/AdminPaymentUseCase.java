package edu.dosw.styleradar.payments.application.usecase;

import edu.dosw.styleradar.payments.application.dto.response.PaymentResponse;
import edu.dosw.styleradar.payments.application.port.in.AdminPaymentPort;
import edu.dosw.styleradar.payments.application.port.out.PaymentOrderRepositoryPort;
import edu.dosw.styleradar.payments.application.port.out.SubscriptionRepositoryPort;
import edu.dosw.styleradar.payments.application.port.out.PlanRepositoryPort;
import edu.dosw.styleradar.payments.domain.model.PaymentStatus;
import edu.dosw.styleradar.payments.domain.model.PaymentOrder;
import edu.dosw.styleradar.payments.domain.exception.PaymentOrderNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminPaymentUseCase implements AdminPaymentPort {

    private final PaymentOrderRepositoryPort paymentOrderRepository;
    private final SubscriptionRepositoryPort subscriptionRepository;
    private final PlanRepositoryPort planRepository;

    public AdminPaymentUseCase(PaymentOrderRepositoryPort paymentOrderRepository,
                               SubscriptionRepositoryPort subscriptionRepository,
                               PlanRepositoryPort planRepository) {
        this.paymentOrderRepository = paymentOrderRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.planRepository = planRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse get(Long id) {
        PaymentOrder order = paymentOrderRepository.findById(id)
                .orElseThrow(PaymentOrderNotFoundException::new);
        return toResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> list(PaymentStatus status) {
        return paymentOrderRepository.list(status).stream()
                .map(this::toResponse)
                .toList();
    }

    private PaymentResponse toResponse(PaymentOrder order) {
        return subscriptionRepository.findById(order.getSubscriptionId())
                .map(subscription -> PaymentResponse.from(
                        order,
                        subscription,
                        planRepository.findById(subscription.getPlanId()).orElse(null)))
                .orElseGet(() -> PaymentResponse.from(order));
    }
}
