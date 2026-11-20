package edu.dosw.styleradar.payments.application.usecase;

import edu.dosw.styleradar.payments.application.dto.response.PaymentResponse;
import edu.dosw.styleradar.payments.application.port.in.AdminPaymentPort;
import edu.dosw.styleradar.payments.application.port.out.PaymentOrderRepositoryPort;
import edu.dosw.styleradar.payments.domain.model.PaymentStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminPaymentUseCase implements AdminPaymentPort {

    private final PaymentOrderRepositoryPort paymentOrderRepository;

    public AdminPaymentUseCase(PaymentOrderRepositoryPort paymentOrderRepository) {
        this.paymentOrderRepository = paymentOrderRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> list(PaymentStatus status) {
        return paymentOrderRepository.list(status).stream()
                .map(PaymentResponse::from)
                .toList();
    }
}
