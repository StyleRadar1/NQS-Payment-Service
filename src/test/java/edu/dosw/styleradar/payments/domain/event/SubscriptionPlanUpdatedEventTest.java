package edu.dosw.styleradar.payments.domain.event;

import edu.dosw.styleradar.payments.TestFixtures;
import edu.dosw.styleradar.payments.domain.model.SubscriberType;
import edu.dosw.styleradar.payments.domain.model.SubscriptionStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SubscriptionPlanUpdatedEventTest {

    @Test
    void createsActivationEventForBuyer() {
        SubscriptionPlanUpdatedEvent event = SubscriptionPlanUpdatedEvent.create(
                TestFixtures.activeUser("10", TestFixtures.HOY, TestFixtures.HOY.plusDays(30)),
                null, "PLAN_RADAR_PLUS", "corr");

        assertThat(event.eventType()).isEqualTo("styleradar.pagos.suscripcion.plan.actualizado");
        assertThat(event.data().subscriberType()).isEqualTo("BUYER");
        assertThat(event.data().previousPlan()).isNull();
        assertThat(event.data().currentPlan()).isEqualTo("PLAN_RADAR_PLUS");
        assertThat(event.correlationId()).isEqualTo("corr");
    }

    @Test
    void createsSuspensionEventForStore() {
        SubscriptionPlanUpdatedEvent event = SubscriptionPlanUpdatedEvent.create(
                TestFixtures.activeStore("20", "900123456", TestFixtures.HOY, TestFixtures.HOY.plusDays(30)),
                "PLAN_STORE_MONTHLY", SubscriptionPlanUpdatedEvent.SUSPENDED, null);

        assertThat(event.data().subscriberType()).isEqualTo("STORE");
        assertThat(event.data().previousPlan()).isEqualTo("PLAN_STORE_MONTHLY");
        assertThat(event.data().currentPlan()).isEqualTo("SUSPENDED");
        assertThat(event.correlationId()).isNotBlank();
    }

    @Test
    void mapsPlanCodes() {
        assertThat(SubscriptionPlanUpdatedEvent.planCode(SubscriberType.USER, 1L)).isEqualTo("PLAN_RADAR");
        assertThat(SubscriptionPlanUpdatedEvent.planCode(SubscriberType.USER, 2L)).isEqualTo("PLAN_RADAR_PLUS");
        assertThat(SubscriptionPlanUpdatedEvent.planCode(SubscriberType.STORE, 3L)).isEqualTo("PLAN_STORE_MONTHLY");
        assertThat(SubscriptionPlanUpdatedEvent.planCode(SubscriberType.USER, null)).isEqualTo("PLAN_RADAR");
    }

    @Test
    void resolvesPreviousPlanForActivation() {
        var activeUser = TestFixtures.activeUser("10", TestFixtures.HOY, TestFixtures.HOY.plusDays(30));
        var activeStore = TestFixtures.activeStore("20", "900123456", TestFixtures.HOY, TestFixtures.HOY.plusDays(30));

        assertThat(SubscriptionPlanUpdatedEvent.previousPlanForActivation(activeUser, SubscriptionStatus.PENDING))
                .isEqualTo("PLAN_RADAR");
        assertThat(SubscriptionPlanUpdatedEvent.previousPlanForActivation(activeUser, SubscriptionStatus.SUSPENDED))
                .isEqualTo(SubscriptionPlanUpdatedEvent.SUSPENDED);
        assertThat(SubscriptionPlanUpdatedEvent.previousPlanForActivation(activeStore, SubscriptionStatus.PENDING))
                .isNull();
    }
}
