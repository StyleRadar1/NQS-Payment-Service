package edu.dosw.styleradar.pagos_suscripciones.infrastructure.adapter.out.cache;

import edu.dosw.styleradar.pagos_suscripciones.domain.port.out.IdempotencyPort;
import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
@Profile("!test")
public class RedisIdempotencyAdapter implements IdempotencyPort {

    private final StringRedisTemplate redisTemplate;

    @Override
    public boolean adquirirLock(String key) {
        String finalKey = key.startsWith("lock:") ? key : "lock:pago_" + key;
        Boolean ok = redisTemplate.opsForValue()
                .setIfAbsent(finalKey, "locked", Duration.ofSeconds(30));
        return Boolean.TRUE.equals(ok);
    }

    @Override
    public void liberarLock(String key) {
        String finalKey = key.startsWith("lock:") ? key : "lock:pago_" + key;
        redisTemplate.delete(finalKey);
    }
}