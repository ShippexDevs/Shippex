package com.shippex.service;

import java.time.Duration;

public interface RedisService {

    void set(String key, Object value, Duration ttl);

    Object get(String key);

    <T> T get(String key, Class<T> clazz);

    void delete(String key);

    boolean exists(String key);

    int incrementRetryCount(String key, Duration ttl);

    long getRemainingTTL(String key);

}
