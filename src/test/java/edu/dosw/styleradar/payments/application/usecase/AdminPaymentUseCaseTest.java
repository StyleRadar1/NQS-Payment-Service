package edu.dosw.styleradar.payments.application.usecase;

import edu.dosw.styleradar.payments.TestFixtures;
import edu.dosw.styleradar.payments.application.dto.response.PaymentResponse;
import edu.dosw.styleradar.payments.application.port.out.PaymentOrderRepositoryPort;
import edu.dosw.styleradar.payments.domain.model.PaymentStatus;
import edu.dosw.styleradar.payments.domain.model.PaymentOrder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminPaymentUseCaseTest {

    @Mock
    private PaymentOrderRepositoryPort paymentOrderRepository;

    @Test
    void listaAllLosPayments() {
        PaymentOrder order = TestFixtures.conIdOrder(TestFixtures.aprobada(100L, "14900"), 1L);
        when(paymentOrderRepository.list(null)).thenReturn(List.of(order));

        List<PaymentResponse> payments = new AdminPaymentUseCase(paymentOrderRepository).list(null);

        assertThat(payments).hasSize(1);
        assertThat(payments.get(0).status()).isEqualTo(PaymentStatus.APPROVED);
        verify(paymentOrderRepository).list(null);
    }

    @Test
    void filtraPorStatus() {
        when(paymentOrderRepository.list(PaymentStatus.REJECTED)).thenReturn(List.of());

        assertThat(new AdminPaymentUseCase(paymentOrderRepository).list(PaymentStatus.REJECTED)).isEmpty();
    }
}
