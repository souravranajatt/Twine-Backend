package com.loginapp.loginapp.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RedisService {

    private final StringRedisTemplate stringRedisTemplate;

    RedisService(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    // Save a key-value pair with an expiration time
    public void setValueWithExpiry(String key, String value, long timeoutInSeconds) {
        stringRedisTemplate.opsForValue().set(key, value, Duration.ofSeconds(timeoutInSeconds));
    }

    // Save a key-value pair without expiration
    public void setValue(String key, String value) {
        stringRedisTemplate.opsForValue().set(key, value);
    }

    // Get a value by key
    public String getValue(String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }

    // Delete a key
    public void deleteKey(String key) {
        stringRedisTemplate.delete(key);
    }

    // Check if a key exists
    public boolean hasKey(String key) {
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(key));
    }

    // Get remaining TTL in seconds
    public Long getExpire(String key) {
        return stringRedisTemplate.getExpire(key);
    }

    // Update value while preserving the remaining TTL
    public void setValueKeepExpire(String key, String value) {
        Long ttl = stringRedisTemplate.getExpire(key);
        if (ttl != null && ttl > 0) {
            stringRedisTemplate.opsForValue().set(key, value, Duration.ofSeconds(ttl));
        } else if (ttl != null && ttl == -1) {
            stringRedisTemplate.opsForValue().set(key, value);
        }
    }
}
