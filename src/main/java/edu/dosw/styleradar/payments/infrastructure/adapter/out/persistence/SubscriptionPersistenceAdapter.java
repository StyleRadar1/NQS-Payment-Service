package edu.dosw.styleradar.payments.infrastructure.adapter.out.persistence;

import edu.dosw.styleradar.payments.application.port.out.SubscriptionRepositoryPort;
import edu.dosw.styleradar.payments.domain.model.SubscriptionStatus;
import edu.dosw.styleradar.payments.domain.model.Subscription;
import edu.dosw.styleradar.payments.infrastructure.adapter.out.persistence.repository.SubscriptionJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SubscriptionPersistenceAdapter implements SubscriptionRepositoryPort {

    private static final List<SubscriptionStatus> ESTADOS_VIGENTES =
            List.of(SubscriptionStatus.ACTIVE, SubscriptionStatus.PENDING);

    private final SubscriptionJpaRepository subscriptionJpaRepository;

    @Override
    public Subscription save(Subscription subscription) {
        return PersistenceMapper.toDomain(
                subscriptionJpaRepository.save(PersistenceMapper.toEntity(subscription)));
    }

    @Override
    public Optional<Subscription> findById(Long id) {
        return subscriptionJpaRepository.findById(id).map(PersistenceMapper::toDomain);
    }

    @Override
    public Optional<Subscription> findLatestBySubscriber(String subscriberId) {
        return subscriptionJpaRepository.findFirstBySubscriberIdOrderByIdDesc(subscriberId)
                .map(PersistenceMapper::toDomain);
    }

    @Override
    public Optional<Subscription> findActiveOrPending(String subscriberId) {
        return subscriptionJpaRepository
                .findFirstBySubscriberIdAndStatusInOrderByIdDesc(subscriberId, ESTADOS_VIGENTES)
                .map(PersistenceMapper::toDomain);
    }

    @Override
    public Optional<Subscription> findByPreapprovalId(String preapprovalId) {
        return subscriptionJpaRepository.findFirstByMercadoPagoPreapprovalId(preapprovalId)
                .map(PersistenceMapper::toDomain);
    }

    @Override
    public List<Subscription> findActiveStores(LocalDate today) {
        return subscriptionJpaRepository
                .findBySubscriberTypeAndStatusAndEndDateGreaterThanEqual(
                        edu.dosw.styleradar.payments.domain.model.SubscriberType.STORE, SubscriptionStatus.ACTIVE, today)
                .stream()
                .map(PersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public List<Subscription> findExpiredActive(LocalDate today) {
        return subscriptionJpaRepository
                .findByStatusAndEndDateBefore(SubscriptionStatus.ACTIVE, today).stream()
                .map(PersistenceMapper::toDomain)
                .toList();
    }
}
