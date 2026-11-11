package edu.dosw.styleradar.pagos_suscripciones.infrastructure.adapter.out.cache;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RedisIdempotencyAdapterTest {

    @Mock private StringRedisTemplate redisTemplate;
    @Mock private ValueOperations<String, String> valueOperations;
    @InjectMocks private RedisIdempotencyAdapter adapter;

    @Test
    void debeAdquirirLockExitosamente() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(anyString(), anyString(), any(Duration.class))).thenReturn(true);

        assertTrue(adapter.adquirirLock("MP-123"));
    }

    @Test
    void noDebeAdquirirLockSiYaExiste() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(anyString(), anyString(), any(Duration.class))).thenReturn(false);

        assertFalse(adapter.adquirirLock("MP-123"));
    }

    @Test
    void debeLiberarLock() {
        adapter.liberarLock("MP-123");
        verify(redisTemplate).delete("lock:webhook:MP-123");
    }
}