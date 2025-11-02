package com.gearup.shared.cache;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisNotificationCache {

    private final RedisTemplate<String, Object> redisTemplate;
    
    private static final String UNREAD_COUNT_PREFIX = "notification:unread:";
    private static final long CACHE_TTL_HOURS = 24;

    /**
     * Get unread count from cache
     */
    public Long getUnreadCount(String userId) {
        try {
            String key = UNREAD_COUNT_PREFIX + userId;
            Object value = redisTemplate.opsForValue().get(key);
            return value != null ? Long.valueOf(value.toString()) : null;
        } catch (NumberFormatException e) {
            log.error("Error getting unread count from Redis for user: {}", userId, e);
            return null;
        }
    }

    /**
     * Set unread count in cache
     */
    public void setUnreadCount(String userId, Long count) {
        try {
            String key = UNREAD_COUNT_PREFIX + userId;
            redisTemplate.opsForValue().set(key, count, CACHE_TTL_HOURS, TimeUnit.HOURS);
        } catch (Exception e) {
            log.error("Error setting unread count in Redis for user: {}", userId, e);
        }
    }

    /**
     * Increment unread count
     */
    public void incrementUnreadCount(String userId) {
        try {
            String key = UNREAD_COUNT_PREFIX + userId;
            redisTemplate.opsForValue().increment(key);
            redisTemplate.expire(key, CACHE_TTL_HOURS, TimeUnit.HOURS);
        } catch (Exception e) {
            log.error("Error incrementing unread count in Redis for user: {}", userId, e);
        }
    }

    /**
     * Decrement unread count
     */
    public void decrementUnreadCount(String userId) {
        try {
            String key = UNREAD_COUNT_PREFIX + userId;
            Long currentCount = getUnreadCount(userId);
            
            if (currentCount != null && currentCount > 0) {
                redisTemplate.opsForValue().decrement(key);
            }
        } catch (Exception e) {
            log.error("Error decrementing unread count in Redis for user: {}", userId, e);
        }
    }

    /**
     * Clear cache for a user
     */
    public void clearCache(String userId) {
        try {
            String key = UNREAD_COUNT_PREFIX + userId;
            redisTemplate.delete(key);
        } catch (Exception e) {
            log.error("Error clearing cache in Redis for user: {}", userId, e);
        }
    }
}
