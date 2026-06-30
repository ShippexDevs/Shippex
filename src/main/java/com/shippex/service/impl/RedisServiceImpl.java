package com.shippex.service.impl;

import com.shippex.service.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisServiceImpl implements RedisService {

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * Save value in Redis with TTL.
     */
    public void set(String key, Object value, Duration ttl) {
        try {
            redisTemplate.opsForValue().set(key, value, ttl);

            log.debug("Redis SET successful. Key={}", key);

        } catch (DataAccessException ex) {
            log.error("Redis SET failed. Key={}", key, ex);
            throw ex;
        } catch (Exception ex) {
            log.error("Unexpected error while saving key={} to Redis", key, ex);
            throw new RuntimeException("Unable to save data in Redis.", ex);
        }
    }

    /**
     * Retrieve value from Redis.
     */
    public Object get(String key) {
        try {

            Object value = redisTemplate.opsForValue().get(key);

            log.debug("Redis GET successful. Key={}, Exists={}", key, value != null);

            return value;

        } catch (DataAccessException ex) {
            log.error("Redis GET failed. Key={}", key, ex);
            throw ex;
        } catch (Exception ex) {
            log.error("Unexpected error while reading key={} from Redis", key, ex);
            throw new RuntimeException("Unable to read data from Redis.", ex);
        }
    }

    /**
     * Retrieve value with expected type.
     */
    public <T> T get(String key, Class<T> clazz) {

        Object value = get(key);

        if (value == null) {
            return null;
        }

        if (!clazz.isInstance(value)) {
            throw new IllegalStateException(
                    String.format(
                            "Expected Redis value of type %s but found %s for key %s",
                            clazz.getSimpleName(),
                            value.getClass().getSimpleName(),
                            key
                    )
            );
        }

        return clazz.cast(value);
    }

    /**
     * Delete a key.
     */
    public void delete(String key) {

        try {

            Boolean deleted = redisTemplate.delete(key);

            log.debug("Redis DELETE. Key={}, Deleted={}", key, deleted);

        } catch (DataAccessException ex) {
            log.error("Redis DELETE failed. Key={}", key, ex);
            throw ex;
        } catch (Exception ex) {
            log.error("Unexpected error while deleting key={}", key, ex);
            throw new RuntimeException("Unable to delete Redis key.", ex);
        }
    }

    /**
     * Check if key exists.
     */
    public boolean exists(String key) {

        try {

            Boolean exists = redisTemplate.hasKey(key);

            return Boolean.TRUE.equals(exists);

        } catch (DataAccessException ex) {
            log.error("Redis EXISTS failed. Key={}", key, ex);
            throw ex;
        } catch (Exception ex) {
            log.error("Unexpected error while checking key={}", key, ex);
            throw new RuntimeException("Unable to check Redis key.", ex);
        }
    }

    /**
     * Increment retry count while preserving TTL.
     *
     * If key doesn't exist:
     * retry = 1
     * TTL = supplied TTL
     */
    public int incrementRetryCount(String key, Duration ttl) {

        try {

            Long value = redisTemplate.opsForValue().increment(key);

            if (value == null) {
                throw new RuntimeException("Redis increment returned null.");
            }

            Long currentTtl = redisTemplate.getExpire(key, TimeUnit.SECONDS);

            /*
             * Set TTL only when key is newly created
             * or TTL somehow disappeared.
             */
            if (currentTtl == null || currentTtl < 0) {
                redisTemplate.expire(key, ttl);
            }

            log.debug("Retry count incremented. Key={}, Count={}", key, value);

            return value.intValue();

        } catch (DataAccessException ex) {
            log.error("Failed to increment retry count. Key={}", key, ex);
            throw ex;
        } catch (Exception ex) {
            log.error("Unexpected error while incrementing retry count. Key={}", key, ex);
            throw new RuntimeException("Unable to update retry count.", ex);
        }
    }

    /**
     * Get remaining TTL in seconds.
     */
    public long getRemainingTTL(String key) {

        try {

            Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);

            return ttl == null ? -1 : ttl;

        } catch (DataAccessException ex) {
            log.error("Failed to fetch TTL. Key={}", key, ex);
            throw ex;
        } catch (Exception ex) {
            log.error("Unexpected error while fetching TTL. Key={}", key, ex);
            throw new RuntimeException("Unable to fetch Redis TTL.", ex);
        }
    }



}