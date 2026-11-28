package edu.dosw.styleradar.payments.infrastructure.adapter.out.cache;

import edu.dosw.styleradar.payments.application.port.out.IdempotencyPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisIdempotencyAdapter implements IdempotencyPort {

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public boolean tryLock(String key, Duration ttl) {
        try {
            Boolean acquired = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", ttl);
            return Boolean.TRUE.equals(acquired);
        } catch (DataAccessException ex) {
            log.warn("Redis unavailable for lock {}; continuing with the database barrier: {}",
                    key, ex.getMessage());
            return true;
        }
    }

    @Override
    public void release(String key) {
        try {
            stringRedisTemplate.delete(key);
        } catch (DataAccessException ex) {
            log.warn("Could not release lock {} in Redis: {}", key, ex.getMessage());
        }
    }
}
