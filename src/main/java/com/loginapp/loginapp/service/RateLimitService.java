package com.loginapp.loginapp.service;

import java.time.Duration;
import java.util.UUID;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RateLimitService {

    private final StringRedisTemplate redisTemplate;

    public RateLimitService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // Sliding window check using Redis Sorted Set (ZSET)
    public RateLimitResult checkLimit(String actionKey, String clientIdentifier, int maxRequests, int windowSeconds) {
        String redisKey = "ratelimit:" + actionKey + ":" + clientIdentifier;
        long now = System.currentTimeMillis();
        long windowStart = now - (windowSeconds * 1000L);

        try {
            // Drop requests that fall outside the current sliding window
            redisTemplate.opsForZSet().removeRangeByScore(redisKey, 0, windowStart);

            // Count how many requests are still in the active window
            Long currentCount = redisTemplate.opsForZSet().zCard(redisKey);
            long count = currentCount != null ? currentCount : 0L;

            // Block request if limit is crossed and calculate wait time
            if (count >= maxRequests) {
                var oldestEntries = redisTemplate.opsForZSet().rangeWithScores(redisKey, 0, 0);
                long retryAfterSeconds = windowSeconds;
                if (oldestEntries != null && !oldestEntries.isEmpty()) {
                    Double oldestScore = oldestEntries.iterator().next().getScore();
                    if (oldestScore != null) {
                        long elapsed = (now - oldestScore.longValue()) / 1000L;
                        retryAfterSeconds = Math.max(1, windowSeconds - elapsed);
                    }
                }
                return new RateLimitResult(false, count, maxRequests, retryAfterSeconds);
            }

            // Record this request with timestamp as score
            String uniqueMember = now + "-" + UUID.randomUUID().toString().substring(0, 6);
            redisTemplate.opsForZSet().add(redisKey, uniqueMember, (double) now);

            // Set TTL so stale keys clean up automatically
            redisTemplate.expire(redisKey, Duration.ofSeconds(windowSeconds + 10));

            return new RateLimitResult(true, count + 1, maxRequests, 0L);

        } catch (Exception e) {
            // Fail open if Redis is down so users don't get blocked
            return new RateLimitResult(true, 0L, maxRequests, 0L);
        }
    }

    public record RateLimitResult(
        boolean allowed,
        long currentRequests,
        long maxRequests,
        long retryAfterSeconds
    ) {}
}
