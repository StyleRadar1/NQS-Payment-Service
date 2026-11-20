package edu.dosw.styleradar.payments.application.port.in;

import edu.dosw.styleradar.payments.application.dto.request.StartSubscriptionRequest;
import edu.dosw.styleradar.payments.application.dto.response.CancellationResponse;
import edu.dosw.styleradar.payments.application.dto.response.StartSubscriptionResponse;
import edu.dosw.styleradar.payments.application.dto.response.SubscriptionResponse;

public interface SubscriptionPort {

    StartSubscriptionResponse start(String subscriberId, StartSubscriptionRequest request);

    SubscriptionResponse get(String subscriberId, String requesterId, String role);

    CancellationResponse cancel(String subscriberId,
                                 boolean confirmed,
                                 String requesterId,
                                 String role);
}
