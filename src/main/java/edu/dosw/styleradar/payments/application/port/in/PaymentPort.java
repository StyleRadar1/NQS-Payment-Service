package edu.dosw.styleradar.payments.application.port.in;

import edu.dosw.styleradar.payments.application.dto.response.PaymentResponse;
import edu.dosw.styleradar.payments.application.dto.response.RetryPaymentResponse;

import java.util.List;

public interface PaymentPort {

    List<PaymentResponse> history(String subscriberId);

    RetryPaymentResponse retry(Long orderId, String payerEmail, String requesterId);
}
