package edu.dosw.styleradar.payments.application.port.out;

import java.util.Map;

public interface WebhookLogPort {

    boolean exists(String externalPaymentId);

    void register(String externalPaymentId, Map<String, Object> payload, String processingStatus);
}
