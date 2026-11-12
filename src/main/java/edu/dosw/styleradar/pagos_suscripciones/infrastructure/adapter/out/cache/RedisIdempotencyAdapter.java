package edu.dosw.styleradar.pagos_suscripciones.infrastructure.adapter.out.cache;

import edu.dosw.styleradar.pagos_suscripciones.domain.port.out.IdempotencyPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
@Profile("!test")
public class RedisIdempotencyAdapter implements IdempotencyPort {

    private final StringRedisTemplate redisTemplate;

    @Override
    public boolean adquirirLock(String key) {
        try {
            String finalKey = key.startsWith("lock:") ? key : "lock:pago_" + key;
            Boolean ok = redisTemplate.opsForValue()
                    .setIfAbsent(finalKey, "locked", Duration.ofSeconds(30));
            return Boolean.TRUE.equals(ok);
        } catch (Exception e) {
            log.warn("Redis no disponible. Omitiendo lock de idempotencia para: {}. Error: {}", key, e.getMessage());
            return true; // Dejamos pasar el pago si Redis está caído
        }
    }

    @Override
    public void liberarLock(String key) {
        try {
            String finalKey = key.startsWith("lock:") ? key : "lock:pago_" + key;
            redisTemplate.delete(finalKey);
        } catch (Exception e) {
            log.warn("No se pudo liberar el lock en Redis: {}", e.getMessage());
        }
    }
}