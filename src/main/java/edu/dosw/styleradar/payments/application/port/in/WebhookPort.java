package edu.dosw.styleradar.payments.application.port.in;

import edu.dosw.styleradar.payments.application.dto.request.NotificationPayment;

public interface WebhookPort {

    void process(NotificationPayment notification, String correlationId);
}
