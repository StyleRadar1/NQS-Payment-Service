package edu.dosw.styleradar.payments.infrastructure.adapter.out.persistence;

import edu.dosw.styleradar.payments.application.port.out.PaymentOrderRepositoryPort;
import edu.dosw.styleradar.payments.domain.model.PaymentStatus;
import edu.dosw.styleradar.payments.domain.model.PaymentOrder;
import edu.dosw.styleradar.payments.infrastructure.adapter.out.persistence.repository.PaymentOrderJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PaymentOrderPersistenceAdapter implements PaymentOrderRepositoryPort {

    private final PaymentOrderJpaRepository paymentOrderJpaRepository;

    @Override
    public PaymentOrder save(PaymentOrder paymentOrder) {
        return PersistenceMapper.toDomain(
                paymentOrderJpaRepository.save(PersistenceMapper.toEntity(paymentOrder)));
    }

    @Override
    public Optional<PaymentOrder> findById(Long id) {
        return paymentOrderJpaRepository.findById(id).map(PersistenceMapper::toDomain);
    }

    @Override
    public Optional<PaymentOrder> findByExternalPaymentId(String externalPaymentId) {
        return paymentOrderJpaRepository.findByExternalPaymentId(externalPaymentId)
                .map(PersistenceMapper::toDomain);
    }

    @Override
    public Optional<PaymentOrder> findPendingBySubscription(Long subscriptionId) {
        return paymentOrderJpaRepository
                .findFirstBySubscriptionIdAndStatusOrderByCreatedAtDesc(subscriptionId, PaymentStatus.PENDING)
                .map(PersistenceMapper::toDomain);
    }

    @Override
    public List<PaymentOrder> findBySubscription(Long subscriptionId) {
        return paymentOrderJpaRepository.findBySubscriptionIdOrderByCreatedAtDesc(subscriptionId)
                .stream().map(PersistenceMapper::toDomain).toList();
    }

    @Override
    public List<PaymentOrder> findBySubscriber(String subscriberId) {
        return paymentOrderJpaRepository
                .findBySubscription_SubscriberIdOrderByCreatedAtDesc(subscriberId)
                .stream().map(PersistenceMapper::toDomain).toList();
    }

    @Override
    public List<PaymentOrder> list(PaymentStatus status) {
        List<edu.dosw.styleradar.payments.infrastructure.adapter.out.persistence.entity.PaymentOrderEntity> entities =
                status == null
                        ? paymentOrderJpaRepository.findAllByOrderByCreatedAtDesc()
                        : paymentOrderJpaRepository.findByStatusOrderByCreatedAtDesc(status);
        return entities.stream().map(PersistenceMapper::toDomain).toList();
    }

    @Override
    public List<PaymentOrder> findPendingBefore(LocalDateTime limit) {
        return paymentOrderJpaRepository
                .findByStatusAndCreatedAtBefore(PaymentStatus.PENDING, limit)
                .stream().map(PersistenceMapper::toDomain).toList();
    }
}
