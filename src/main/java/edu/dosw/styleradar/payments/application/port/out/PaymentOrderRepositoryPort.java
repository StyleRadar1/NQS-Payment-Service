package edu.dosw.styleradar.payments.application.port.out;

import edu.dosw.styleradar.payments.domain.model.PaymentStatus;
import edu.dosw.styleradar.payments.domain.model.PaymentOrder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PaymentOrderRepositoryPort {

    PaymentOrder save(PaymentOrder paymentOrder);

    Optional<PaymentOrder> findById(Long id);

    Optional<PaymentOrder> findByExternalPaymentId(String externalPaymentId);

    Optional<PaymentOrder> findPendingBySubscription(Long subscriptionId);

    List<PaymentOrder> findBySubscription(Long subscriptionId);

    
    List<PaymentOrder> findBySubscriber(String subscriberId);

    
    List<PaymentOrder> list(PaymentStatus status);

    
    List<PaymentOrder> findPendingBefore(LocalDateTime limit);
}
