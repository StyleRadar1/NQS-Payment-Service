package edu.dosw.styleradar.payments.application.service;

import edu.dosw.styleradar.payments.TestFixtures;
import edu.dosw.styleradar.payments.application.port.out.EventPublisherPort;
import edu.dosw.styleradar.payments.application.port.out.SubscriptionRepositoryPort;
import edu.dosw.styleradar.payments.domain.event.SubscriptionSuspendedEvent;
import edu.dosw.styleradar.payments.domain.model.SubscriptionStatus;
import edu.dosw.styleradar.payments.domain.model.Subscription;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubscriptionSchedulerTest {

    @Mock
    private SubscriptionRepositoryPort subscriptionRepository;
    @Mock
    private EventPublisherPort eventPublisher;

    private SubscriptionScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new SubscriptionScheduler(subscriptionRepository, eventPublisher, TestFixtures.CLOCK);
    }

    @Test
    void suspendeExpiredYPublicaEvento() {
        Subscription vencida = TestFixtures.activeStore("STORE-1", "900123456",
                TestFixtures.HOY.minusMonths(1), TestFixtures.HOY.minusDays(1));
        when(subscriptionRepository.findExpiredActive(TestFixtures.HOY)).thenReturn(List.of(vencida));
        when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(inv -> inv.getArgument(0));

        scheduler.revisarSubscriptionsExpired();

        assertThat(vencida.getStatus()).isEqualTo(SubscriptionStatus.SUSPENDED);
        assertThat(vencida.isAutoRenewal()).isFalse();
        ArgumentCaptor<SubscriptionSuspendedEvent> evento =
                ArgumentCaptor.forClass(SubscriptionSuspendedEvent.class);
        verify(eventPublisher).publishSubscriptionSuspended(evento.capture());
        assertThat(evento.getValue().data().subscriberId()).isEqualTo("STORE-1");
        assertThat(evento.getValue().data().nit()).isEqualTo("900123456");
        assertThat(evento.getValue().data().reason()).isEqualTo("FALTA_DE_PAGO");
    }

    @Test
    void noHaceNadaSiNoHayExpired() {
        when(subscriptionRepository.findExpiredActive(TestFixtures.HOY)).thenReturn(List.of());

        scheduler.revisarSubscriptionsExpired();

        verify(subscriptionRepository, never()).save(any());
        verify(eventPublisher, never()).publishSubscriptionSuspended(any());
    }

    @Test
    void continuaConLasDemasSiUnaFalla() {
        Subscription primera = TestFixtures.activeUser("USER-1",
                TestFixtures.HOY.minusMonths(1), TestFixtures.HOY.plusDays(1));
        Subscription segunda = TestFixtures.activeUser("USER-2",
                TestFixtures.HOY.minusMonths(1), TestFixtures.HOY.minusDays(1));
        when(subscriptionRepository.findExpiredActive(TestFixtures.HOY))
                .thenReturn(List.of(primera, segunda));
        when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(inv -> inv.getArgument(0));

        scheduler.revisarSubscriptionsExpired();

        verify(subscriptionRepository, times(1)).save(segunda);
        verify(eventPublisher, times(1)).publishSubscriptionSuspended(any());
    }

    @Test
    void capturaFalloDelBrokerSinRomperElJob() {
        Subscription vencida = TestFixtures.activeUser("USER-1",
                TestFixtures.HOY.minusMonths(1), TestFixtures.HOY.minusDays(1));
        when(subscriptionRepository.findExpiredActive(TestFixtures.HOY)).thenReturn(List.of(vencida));
        when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(inv -> inv.getArgument(0));
        doThrow(new IllegalStateException("Rabbit caído"))
                .when(eventPublisher).publishSubscriptionSuspended(any());

        scheduler.revisarSubscriptionsExpired();

        assertThat(vencida.getStatus()).isEqualTo(SubscriptionStatus.SUSPENDED);
    }
}
