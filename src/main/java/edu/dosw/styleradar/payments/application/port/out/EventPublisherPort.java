package edu.dosw.styleradar.payments.application.port.out;

import edu.dosw.styleradar.payments.domain.event.PaymentConfirmedEvent;
import edu.dosw.styleradar.payments.domain.event.SubscriptionPlanUpdatedEvent;
import edu.dosw.styleradar.payments.domain.event.SubscriptionSuspendedEvent;

public interface EventPublisherPort {

    void publishPaymentConfirmed(PaymentConfirmedEvent event);

    void publishSubscriptionSuspended(SubscriptionSuspendedEvent event);

    void publishSubscriptionPlanUpdated(SubscriptionPlanUpdatedEvent event);
}
