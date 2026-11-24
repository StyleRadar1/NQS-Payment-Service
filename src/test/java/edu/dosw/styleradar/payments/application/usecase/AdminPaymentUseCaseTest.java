package edu.dosw.styleradar.payments.application.usecase;

import edu.dosw.styleradar.payments.TestFixtures;
import edu.dosw.styleradar.payments.application.dto.response.PaymentResponse;
import edu.dosw.styleradar.payments.application.port.out.PaymentOrderRepositoryPort;
import edu.dosw.styleradar.payments.application.port.out.SubscriptionRepositoryPort;
import edu.dosw.styleradar.payments.application.port.out.PlanRepositoryPort;
import edu.dosw.styleradar.payments.domain.model.PaymentStatus;
import java.util.Optional;
import edu.dosw.styleradar.payments.domain.model.PaymentOrder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import edu.dosw.styleradar.payments.domain.exception.PaymentOrderNotFoundException;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminPaymentUseCaseTest {

    @Mock
    private PaymentOrderRepositoryPort paymentOrderRepository;
    @Mock
    private SubscriptionRepositoryPort subscriptionRepository;
    @Mock
    private PlanRepositoryPort planRepository;

    @Test
    void listaAllLosPayments() {
        PaymentOrder order = TestFixtures.conIdOrder(TestFixtures.aprobada(100L, "14900"), 1L);
        when(paymentOrderRepository.list(null)).thenReturn(List.of(order));
        when(subscriptionRepository.findById(100L)).thenReturn(Optional.of(
                TestFixtures.activeUser("USER-1", TestFixtures.HOY.minusDays(5), TestFixtures.HOY.plusDays(25))));
        when(planRepository.findById(2L)).thenReturn(Optional.of(TestFixtures.planRadarPlus()));

        List<PaymentResponse> payments = new AdminPaymentUseCase(paymentOrderRepository, subscriptionRepository, planRepository).list(null);

        assertThat(payments).hasSize(1);
        assertThat(payments.get(0).status()).isEqualTo(PaymentStatus.APPROVED);
        assertThat(payments.get(0).planName()).isEqualTo("Radar+");
        assertThat(payments.get(0).subscriberId()).isEqualTo("USER-1");
        assertThat(payments.get(0).subscriberType()).isEqualTo(edu.dosw.styleradar.payments.domain.model.SubscriberType.USER);
        verify(paymentOrderRepository).list(null);
    }

    @Test
    void obtieneDetallePorId() {
        PaymentOrder order = TestFixtures.conIdOrder(TestFixtures.aprobada(100L, "14900"), 1L);
        when(paymentOrderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(subscriptionRepository.findById(100L)).thenReturn(Optional.of(
                TestFixtures.activeUser("USER-1", TestFixtures.HOY.minusDays(5), TestFixtures.HOY.plusDays(25))));
        when(planRepository.findById(2L)).thenReturn(Optional.of(TestFixtures.planRadarPlus()));

        PaymentResponse response = new AdminPaymentUseCase(paymentOrderRepository, subscriptionRepository, planRepository).get(1L);

        assertThat(response.planName()).isEqualTo("Radar+");
        assertThat(response.subscriberId()).isEqualTo("USER-1");
        assertThat(response.subscriberType()).isEqualTo(edu.dosw.styleradar.payments.domain.model.SubscriberType.USER);
        assertThatThrownBy(() -> new AdminPaymentUseCase(paymentOrderRepository, subscriptionRepository, planRepository).get(99L))
                .isInstanceOf(PaymentOrderNotFoundException.class);
    }

    @Test
    void filtraPorStatus() {
        when(paymentOrderRepository.list(PaymentStatus.REJECTED)).thenReturn(List.of());

        assertThat(new AdminPaymentUseCase(paymentOrderRepository, subscriptionRepository, planRepository).list(PaymentStatus.REJECTED)).isEmpty();
    }
}
