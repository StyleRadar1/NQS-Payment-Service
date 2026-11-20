package edu.dosw.styleradar.payments.application.port.out;

import java.time.Duration;

public interface IdempotencyPort {

    boolean tryLock(String key, Duration ttl);

    void release(String key);
}
