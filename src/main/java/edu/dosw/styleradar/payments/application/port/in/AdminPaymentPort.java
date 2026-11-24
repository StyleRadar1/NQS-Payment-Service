package edu.dosw.styleradar.payments.application.port.in;

import edu.dosw.styleradar.payments.application.dto.response.PaymentResponse;
import edu.dosw.styleradar.payments.domain.model.PaymentStatus;

import java.util.List;

public interface AdminPaymentPort {

    List<PaymentResponse> list(PaymentStatus status);

    PaymentResponse get(Long id);
}
