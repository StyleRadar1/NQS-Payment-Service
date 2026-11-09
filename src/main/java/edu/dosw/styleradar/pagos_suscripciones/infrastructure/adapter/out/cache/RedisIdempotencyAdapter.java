package edu.dosw.styleradar.pagos_suscripciones.infrastructure.adapter.out.cache;

import edu.dosw.styleradar.pagos_suscripciones.domain.port.out.IdempotencyPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class RedisIdempotencyAdapter implements IdempotencyPort {

    private final StringRedisTemplate redisTemplate;

    @Override
    public boolean adquirirLock(String idPagoExterno) {
        String key = "lock:webhook:" + idPagoExterno;
        // SETNX con expiración de 30 segundos
        Boolean success = redisTemplate.opsForValue()
                .setIfAbsent(key, "locked", Duration.ofSeconds(30));
        return Boolean.TRUE.equals(success);
    }

    @Override
    public void liberarLock(String idPagoExterno) {
        String key = "lock:webhook:" + idPagoExterno;
        redisTemplate.delete(key);
    }
}