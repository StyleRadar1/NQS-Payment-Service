package edu.dosw.styleradar.pagos.infrastructure.adapter.out.cache;

import edu.dosw.styleradar.pagos.application.port.out.IdempotencyPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Bloqueo distribuido de idempotencia con Redis SETNX (ADR-06).
 *
 * <p>SR-RNF-14: evita que dos entregas simultáneas del mismo webhook
 * procesen el pago dos veces. Si Redis no está disponible se degrada a la
 * restricción única de {@code webhooks_log}, que sigue protegiendo la
 * idempotencia.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisIdempotencyAdapter implements IdempotencyPort {

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public boolean intentarBloquear(String clave, Duration ttl) {
        try {
            Boolean adquirido = stringRedisTemplate.opsForValue().setIfAbsent(clave, "1", ttl);
            return Boolean.TRUE.equals(adquirido);
        } catch (DataAccessException ex) {
            log.warn("Redis no disponible para el lock {}; se continúa con la barrera de BD: {}",
                    clave, ex.getMessage());
            return true;
        }
    }

    @Override
    public void liberar(String clave) {
        try {
            stringRedisTemplate.delete(clave);
        } catch (DataAccessException ex) {
            log.warn("No fue posible liberar el lock {} en Redis: {}", clave, ex.getMessage());
        }
    }
}
