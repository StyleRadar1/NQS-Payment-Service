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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RedisIdempotencyAdapterTest {

    @Mock private StringRedisTemplate redisTemplate;
    @Mock private ValueOperations<String, String> ops;
    @InjectMocks private RedisIdempotencyAdapter adapter;

    @Test
    void debeAdquirirLockConKeyQueYaTienePrefijo() {
        when(redisTemplate.opsForValue()).thenReturn(ops);
        when(ops.setIfAbsent(eq("lock:pago_MP-123"), anyString(), any(Duration.class)))
                .thenReturn(true);

        assertTrue(adapter.adquirirLock("lock:pago_MP-123"));
    }

    @Test
    void debeAgregarPrefijoSiKeyNoLoTrae() {
        when(redisTemplate.opsForValue()).thenReturn(ops);
        when(ops.setIfAbsent(eq("lock:pago_MP-123"), anyString(), any(Duration.class)))
                .thenReturn(true);

        assertTrue(adapter.adquirirLock("MP-123"));
    }

    @Test
    void noDebeAdquirirLockSiYaExiste() {
        when(redisTemplate.opsForValue()).thenReturn(ops);
        when(ops.setIfAbsent(anyString(), anyString(), any(Duration.class))).thenReturn(false);

        assertFalse(adapter.adquirirLock("MP-123"));
    }

    @Test
    void debeLiberarLockConKeyQueYaTienePrefijo() {
        adapter.liberarLock("lock:pago_MP-123");
        verify(redisTemplate).delete("lock:pago_MP-123");
    }

    @Test
    void debeLiberarLockAgregandoPrefijoSiFalta() {
        adapter.liberarLock("MP-123");
        verify(redisTemplate).delete("lock:pago_MP-123");
    }
}