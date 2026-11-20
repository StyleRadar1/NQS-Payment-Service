package edu.dosw.styleradar.payments.application.service;

import edu.dosw.styleradar.payments.application.port.out.EventPublisherPort;
import edu.dosw.styleradar.payments.application.port.out.SubscriptionRepositoryPort;
import edu.dosw.styleradar.payments.domain.event.SubscriptionSuspendedEvent;
import edu.dosw.styleradar.payments.domain.model.Subscription;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "styleradar.scheduling", name = "enabled",
        havingValue = "true", matchIfMissing = true)
public class SubscriptionScheduler {

    private final SubscriptionRepositoryPort subscriptionRepository;
    private final EventPublisherPort eventPublisher;
    private final Clock clock;

    public SubscriptionScheduler(SubscriptionRepositoryPort subscriptionRepository,
                                EventPublisherPort eventPublisher,
                                Clock clock) {
        this.subscriptionRepository = subscriptionRepository;
        this.eventPublisher = eventPublisher;
        this.clock = clock;
    }

    @Scheduled(cron = "${styleradar.scheduling.suspension-cron:0 10 0 * * *}")
    public void revisarSubscriptionsExpired() {
        LocalDate today = LocalDate.now(clock);
        List<Subscription> expired = subscriptionRepository.findExpiredActive(today);
        int suspendeds = 0;
        for (Subscription subscription : expired) {
            try {
                subscription.suspendForNonPayment(today);
                Subscription guardada = subscriptionRepository.save(subscription);
                eventPublisher.publishSubscriptionSuspended(SubscriptionSuspendedEvent.create(
                        new SubscriptionSuspendedEvent.Data(
                                guardada.getId(),
                                guardada.getSubscriberId(),
                                guardada.getSubscriberType().legacyValue(),
                                guardada.getPlanId(),
                                guardada.getTaxId(),
                                guardada.getStoreId(),
                                guardada.getEndDate(),
                                "FALTA_DE_PAGO"),
                        null));
                suspendeds++;
            } catch (RuntimeException ex) {
                log.error("Could not suspend subscription {}: {}",
                        subscription.getId(), ex.getMessage());
            }
        }
        log.info("Daily suspension: {} expired subscriptions processed, {} suspended",
                expired.size(), suspendeds);
    }
}
