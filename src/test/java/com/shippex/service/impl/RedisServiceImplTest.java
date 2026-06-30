package com.shippex.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RedisServiceImplTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private RedisServiceImpl redisService;

    // ============================================================
    // set()
    // ============================================================

    @Test
    void set_shouldStoreValueSuccessfully() {

        when(redisTemplate.opsForValue())
                .thenReturn(valueOperations);

        redisService.set(
                "OTP:test",
                "123456",
                Duration.ofMinutes(5));

        verify(redisTemplate).opsForValue();

        verify(valueOperations).set(
                "OTP:test",
                "123456",
                Duration.ofMinutes(5));
    }

    @Test
    void set_shouldRethrowDataAccessException() {

        when(redisTemplate.opsForValue())
                .thenReturn(valueOperations);

        doThrow(new DataAccessResourceFailureException("Redis Down"))
                .when(valueOperations)
                .set(anyString(), any(), any(Duration.class));

        assertThatThrownBy(() ->
                redisService.set(
                        "OTP:test",
                        "123456",
                        Duration.ofMinutes(5)))
                .isInstanceOf(DataAccessResourceFailureException.class)
                .hasMessageContaining("Redis Down");
    }

    @Test
    void set_shouldWrapUnexpectedException() {

        when(redisTemplate.opsForValue())
                .thenReturn(valueOperations);

        doThrow(new RuntimeException("Unexpected"))
                .when(valueOperations)
                .set(anyString(), any(), any(Duration.class));

        assertThatThrownBy(() ->
                redisService.set(
                        "OTP:test",
                        "123456",
                        Duration.ofMinutes(5)))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Unable to save data in Redis.")
                .hasCauseInstanceOf(RuntimeException.class);
    }

    @Test
    void set_shouldCallSetExactlyOnce() {

        when(redisTemplate.opsForValue())
                .thenReturn(valueOperations);

        redisService.set(
                "OTP:test",
                "123456",
                Duration.ofMinutes(5));

        verify(valueOperations, times(1))
                .set(
                        anyString(),
                        any(),
                        any(Duration.class));

        verifyNoMoreInteractions(valueOperations);
    }


    // ============================================================
    // get(String key)
    // ============================================================

    @Test
    void get_shouldReturnValueSuccessfully() {

        when(redisTemplate.opsForValue())
                .thenReturn(valueOperations);

        when(valueOperations.get("OTP:test"))
                .thenReturn("123456");

        Object value = redisService.get("OTP:test");

        assertThat(value).isEqualTo("123456");

        verify(redisTemplate).opsForValue();
        verify(valueOperations).get("OTP:test");
    }

    @Test
    void get_shouldReturnNullWhenKeyDoesNotExist() {

        when(redisTemplate.opsForValue())
                .thenReturn(valueOperations);

        when(valueOperations.get("OTP:test"))
                .thenReturn(null);

        Object value = redisService.get("OTP:test");

        assertThat(value).isNull();

        verify(valueOperations).get("OTP:test");
    }

    @Test
    void get_shouldRethrowDataAccessException() {

        when(redisTemplate.opsForValue())
                .thenReturn(valueOperations);

        when(valueOperations.get(anyString()))
                .thenThrow(new DataAccessResourceFailureException("Redis Down"));

        assertThatThrownBy(() ->
                redisService.get("OTP:test"))
                .isInstanceOf(DataAccessResourceFailureException.class)
                .hasMessageContaining("Redis Down");
    }

    @Test
    void get_shouldWrapUnexpectedException() {

        when(redisTemplate.opsForValue())
                .thenReturn(valueOperations);

        when(valueOperations.get(anyString()))
                .thenThrow(new RuntimeException("Unexpected"));

        assertThatThrownBy(() ->
                redisService.get("OTP:test"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Unable to read data from Redis.")
                .hasCauseInstanceOf(RuntimeException.class);
    }

    // ============================================================
    // get(String key, Class<T>)
    // ============================================================

    @Test
    void getWithType_shouldReturnString() {

        when(redisTemplate.opsForValue())
                .thenReturn(valueOperations);

        when(valueOperations.get("OTP:test"))
                .thenReturn("123456");

        String value = redisService.get("OTP:test", String.class);

        assertThat(value).isEqualTo("123456");
    }

    @Test
    void getWithType_shouldReturnInteger() {

        when(redisTemplate.opsForValue())
                .thenReturn(valueOperations);

        when(valueOperations.get("COUNT"))
                .thenReturn(5);

        Integer value = redisService.get("COUNT", Integer.class);

        assertThat(value).isEqualTo(5);
    }

    @Test
    void getWithType_shouldReturnBoolean() {

        when(redisTemplate.opsForValue())
                .thenReturn(valueOperations);

        when(valueOperations.get("VERIFIED"))
                .thenReturn(Boolean.TRUE);

        Boolean value = redisService.get("VERIFIED", Boolean.class);

        assertThat(value).isTrue();
    }

    @Test
    void getWithType_shouldReturnNullWhenRedisReturnsNull() {

        when(redisTemplate.opsForValue())
                .thenReturn(valueOperations);

        when(valueOperations.get("OTP:test"))
                .thenReturn(null);

        String value = redisService.get("OTP:test", String.class);

        assertThat(value).isNull();
    }

    @Test
    void getWithType_shouldThrowIllegalStateExceptionWhenTypeDoesNotMatch() {

        when(redisTemplate.opsForValue())
                .thenReturn(valueOperations);

        when(valueOperations.get("OTP:test"))
                .thenReturn(123456);

        assertThatThrownBy(() ->
                redisService.get("OTP:test", String.class))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Expected Redis value of type")
                .hasMessageContaining("String")
                .hasMessageContaining("Integer");
    }



    // ============================================================
    // delete()
    // ============================================================

    @Test
    void delete_shouldDeleteKeySuccessfully() {

        when(redisTemplate.delete("OTP:test"))
                .thenReturn(Boolean.TRUE);

        redisService.delete("OTP:test");

        verify(redisTemplate).delete("OTP:test");
    }

    @Test
    void delete_shouldHandleDeleteReturningFalse() {

        when(redisTemplate.delete("OTP:test"))
                .thenReturn(Boolean.FALSE);

        redisService.delete("OTP:test");

        verify(redisTemplate).delete("OTP:test");
    }

    @Test
    void delete_shouldRethrowDataAccessException() {

        when(redisTemplate.delete(anyString()))
                .thenThrow(new DataAccessResourceFailureException("Redis Down"));

        assertThatThrownBy(() ->
                redisService.delete("OTP:test"))
                .isInstanceOf(DataAccessResourceFailureException.class)
                .hasMessageContaining("Redis Down");
    }

    @Test
    void delete_shouldWrapUnexpectedException() {

        when(redisTemplate.delete(anyString()))
                .thenThrow(new RuntimeException("Unexpected"));

        assertThatThrownBy(() ->
                redisService.delete("OTP:test"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Unable to delete Redis key.")
                .hasCauseInstanceOf(RuntimeException.class);
    }

    // ============================================================
    // exists()
    // ============================================================

    @Test
    void exists_shouldReturnTrue() {

        when(redisTemplate.hasKey("OTP:test"))
                .thenReturn(Boolean.TRUE);

        boolean exists = redisService.exists("OTP:test");

        assertThat(exists).isTrue();

        verify(redisTemplate).hasKey("OTP:test");
    }

    @Test
    void exists_shouldReturnFalse() {

        when(redisTemplate.hasKey("OTP:test"))
                .thenReturn(Boolean.FALSE);

        boolean exists = redisService.exists("OTP:test");

        assertThat(exists).isFalse();
    }

    @Test
    void exists_shouldReturnFalseWhenRedisReturnsNull() {

        when(redisTemplate.hasKey("OTP:test"))
                .thenReturn(null);

        boolean exists = redisService.exists("OTP:test");

        assertThat(exists).isFalse();
    }

    @Test
    void exists_shouldRethrowDataAccessException() {

        when(redisTemplate.hasKey(anyString()))
                .thenThrow(new DataAccessResourceFailureException("Redis Down"));

        assertThatThrownBy(() ->
                redisService.exists("OTP:test"))
                .isInstanceOf(DataAccessResourceFailureException.class)
                .hasMessageContaining("Redis Down");
    }

    @Test
    void exists_shouldWrapUnexpectedException() {

        when(redisTemplate.hasKey(anyString()))
                .thenThrow(new RuntimeException("Unexpected"));

        assertThatThrownBy(() ->
                redisService.exists("OTP:test"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Unable to check Redis key.")
                .hasCauseInstanceOf(RuntimeException.class);
    }

    // ============================================================
    // incrementRetryCount()
    // ============================================================

    @Test
    void incrementRetryCount_shouldIncrementWithoutChangingExistingTtl() {

        when(redisTemplate.opsForValue())
                .thenReturn(valueOperations);

        when(valueOperations.increment("RETRY:test"))
                .thenReturn(2L);

        when(redisTemplate.getExpire("RETRY:test", TimeUnit.SECONDS))
                .thenReturn(120L);

        int count = redisService.incrementRetryCount(
                "RETRY:test",
                Duration.ofMinutes(5));

        assertThat(count).isEqualTo(2);

        verify(redisTemplate, never())
                .expire(anyString(), any(Duration.class));
    }

    @Test
    void incrementRetryCount_shouldSetTtlWhenKeyIsNew() {

        when(redisTemplate.opsForValue())
                .thenReturn(valueOperations);

        when(valueOperations.increment("RETRY:test"))
                .thenReturn(1L);

        when(redisTemplate.getExpire("RETRY:test", TimeUnit.SECONDS))
                .thenReturn(-1L);

        int count = redisService.incrementRetryCount(
                "RETRY:test",
                Duration.ofMinutes(5));

        assertThat(count).isEqualTo(1);

        verify(redisTemplate)
                .expire("RETRY:test", Duration.ofMinutes(5));
    }

    @Test
    void incrementRetryCount_shouldSetTtlWhenRedisReturnsNullTtl() {

        when(redisTemplate.opsForValue())
                .thenReturn(valueOperations);

        when(valueOperations.increment("RETRY:test"))
                .thenReturn(1L);

        when(redisTemplate.getExpire("RETRY:test", TimeUnit.SECONDS))
                .thenReturn(null);

        redisService.incrementRetryCount(
                "RETRY:test",
                Duration.ofMinutes(5));

        verify(redisTemplate)
                .expire("RETRY:test", Duration.ofMinutes(5));
    }

    @Test
    void incrementRetryCount_shouldThrowWhenIncrementReturnsNull() {

        when(redisTemplate.opsForValue())
                .thenReturn(valueOperations);

        when(valueOperations.increment("RETRY:test"))
                .thenReturn(null);

        assertThatThrownBy(() ->
                redisService.incrementRetryCount(
                        "RETRY:test",
                        Duration.ofMinutes(5)))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Unable to update retry count.")
                .hasCauseInstanceOf(RuntimeException.class);
    }

    @Test
    void incrementRetryCount_shouldRethrowDataAccessException() {

        when(redisTemplate.opsForValue())
                .thenReturn(valueOperations);

        when(valueOperations.increment(anyString()))
                .thenThrow(new DataAccessResourceFailureException("Redis Down"));

        assertThatThrownBy(() ->
                redisService.incrementRetryCount(
                        "RETRY:test",
                        Duration.ofMinutes(5)))
                .isInstanceOf(DataAccessResourceFailureException.class)
                .hasMessageContaining("Redis Down");
    }

    @Test
    void incrementRetryCount_shouldWrapUnexpectedException() {

        when(redisTemplate.opsForValue())
                .thenReturn(valueOperations);

        when(valueOperations.increment(anyString()))
                .thenThrow(new RuntimeException("Unexpected"));

        assertThatThrownBy(() ->
                redisService.incrementRetryCount(
                        "RETRY:test",
                        Duration.ofMinutes(5)))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Unable to update retry count.")
                .hasCauseInstanceOf(RuntimeException.class);
    }

    // ============================================================
    // getRemainingTTL()
    // ============================================================

    @Test
    void getRemainingTTL_shouldReturnTtl() {

        when(redisTemplate.getExpire("OTP:test", TimeUnit.SECONDS))
                .thenReturn(240L);

        long ttl = redisService.getRemainingTTL("OTP:test");

        assertThat(ttl).isEqualTo(240L);
    }

    @Test
    void getRemainingTTL_shouldReturnMinusOneWhenRedisReturnsNull() {

        when(redisTemplate.getExpire("OTP:test", TimeUnit.SECONDS))
                .thenReturn(null);

        long ttl = redisService.getRemainingTTL("OTP:test");

        assertThat(ttl).isEqualTo(-1L);
    }

    @Test
    void getRemainingTTL_shouldRethrowDataAccessException() {

        when(redisTemplate.getExpire(anyString(), eq(TimeUnit.SECONDS)))
                .thenThrow(new DataAccessResourceFailureException("Redis Down"));

        assertThatThrownBy(() ->
                redisService.getRemainingTTL("OTP:test"))
                .isInstanceOf(DataAccessResourceFailureException.class)
                .hasMessageContaining("Redis Down");
    }

    @Test
    void getRemainingTTL_shouldWrapUnexpectedException() {

        when(redisTemplate.getExpire(anyString(), eq(TimeUnit.SECONDS)))
                .thenThrow(new RuntimeException("Unexpected"));

        assertThatThrownBy(() ->
                redisService.getRemainingTTL("OTP:test"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Unable to fetch Redis TTL.")
                .hasCauseInstanceOf(RuntimeException.class);
    }
}