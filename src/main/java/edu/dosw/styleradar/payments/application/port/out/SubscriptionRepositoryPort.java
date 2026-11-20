package edu.dosw.styleradar.payments.application.port.out;

import edu.dosw.styleradar.payments.domain.model.Subscription;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SubscriptionRepositoryPort {

    Subscription save(Subscription subscription);

    Optional<Subscription> findById(Long id);

    Optional<Subscription> findLatestBySubscriber(String subscriberId);

    
    Optional<Subscription> findActiveOrPending(String subscriberId);

    Optional<Subscription> findByPreapprovalId(String preapprovalId);

    
    List<Subscription> findExpiredActive(LocalDate today);
}
