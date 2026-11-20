package edu.dosw.styleradar.payments.application.dto.request;

import java.util.Map;

public record NotificationPayment(
        String paymentId,
        String type,
        String requestId,
        Map<String, Object> payload) {
}
