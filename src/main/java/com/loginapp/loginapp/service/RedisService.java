package com.loginapp.loginapp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RedisService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

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
}
