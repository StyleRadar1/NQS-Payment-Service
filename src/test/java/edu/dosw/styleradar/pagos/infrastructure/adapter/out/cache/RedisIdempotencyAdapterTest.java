package edu.dosw.styleradar.pagos.infrastructure.adapter.out.cache;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("RedisIdempotencyAdapter (ADR-06, SR-RNF-14)")
class RedisIdempotencyAdapterTest {

    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;

    private RedisIdempotencyAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new RedisIdempotencyAdapter(redisTemplate);
    }

    @Test
    void adquiereElLockSiNoExiste() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent("lock:pago_1", "1", Duration.ofSeconds(30)))
                .thenReturn(true);

        assertThat(adapter.intentarBloquear("lock:pago_1", Duration.ofSeconds(30))).isTrue();
    }

    @Test
    void noAdquiereElLockSiYaExiste() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(anyString(), anyString(), any(Duration.class)))
                .thenReturn(false);

        assertThat(adapter.intentarBloquear("lock:pago_1", Duration.ofSeconds(30))).isFalse();
    }

    @Test
    void siRedisRespondeNuloSeConsideraNoAdquirido() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(anyString(), anyString(), any(Duration.class)))
                .thenReturn(null);

        assertThat(adapter.intentarBloquear("lock:pago_1", Duration.ofSeconds(30))).isFalse();
    }

    @Test
    void degradaSiRedisNoEstaDisponible() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(anyString(), anyString(), any(Duration.class)))
                .thenThrow(new RedisConnectionFailureException("caído"));

        assertThat(adapter.intentarBloquear("lock:pago_1", Duration.ofSeconds(30))).isTrue();
    }

    @Test
    void liberaElLockYSoportaFalloDeRedis() {
        adapter.liberar("lock:pago_1");
        verify(redisTemplate).delete("lock:pago_1");

        doThrow(new RedisConnectionFailureException("caído")).when(redisTemplate).delete(anyString());
        adapter.liberar("lock:pago_2");
    }
}
