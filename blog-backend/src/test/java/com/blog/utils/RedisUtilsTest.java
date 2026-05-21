package com.blog.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class RedisUtilsTest {

    private RedisUtils redisUtils;
    private RedisTemplate<String, Object> redisTemplate;
    private ValueOperations<String, Object> valueOps;
    private ZSetOperations<String, Object> zSetOps;

    @BeforeEach
    void setUp() {
        redisUtils = new RedisUtils();
        redisTemplate = mock(RedisTemplate.class);
        valueOps = mock(ValueOperations.class);
        zSetOps = mock(ZSetOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(redisTemplate.opsForZSet()).thenReturn(zSetOps);
        when(redisTemplate.getConnectionFactory()).thenReturn(mock(RedisConnectionFactory.class));
        // Inject via reflection
        try {
            var field = RedisUtils.class.getDeclaredField("redisTemplate");
            field.setAccessible(true);
            field.set(redisUtils, redisTemplate);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // ========== set / get ==========

    @Test
    void set_get_withRedis() {
        when(valueOps.get("k1")).thenReturn("v1");
        redisUtils.set("k1", "v1", 60, TimeUnit.SECONDS);
        assertEquals("v1", redisUtils.get("k1"));
        verify(valueOps).set("k1", "v1", 60, TimeUnit.SECONDS);
    }

    @Test
    void set_get_fallbackToMemory() {
        when(redisTemplate.opsForValue()).thenThrow(new RuntimeException("Redis down"));
        redisUtils.set("mem1", "val1", 60, TimeUnit.SECONDS);
        assertEquals("val1", redisUtils.get("mem1"));
    }

    @Test
    void get_expired_returnsNull() {
        redisUtils.set("exp1", "val", 1, TimeUnit.MILLISECONDS);
        // Wait for expiry
        try { Thread.sleep(10); } catch (InterruptedException ignored) {}
        assertNull(redisUtils.get("exp1"));
    }

    // ========== delete ==========

    @Test
    void delete_removesKey() {
        when(redisTemplate.delete("k1")).thenReturn(true);
        assertTrue(redisUtils.delete("k1"));
        verify(redisTemplate).delete("k1");
    }

    // ========== hasKey ==========

    @Test
    void hasKey_exists() {
        when(redisTemplate.hasKey("k1")).thenReturn(true);
        assertTrue(redisUtils.hasKey("k1"));
    }

    @Test
    void hasKey_notExists() {
        when(redisTemplate.hasKey("k1")).thenReturn(false);
        assertFalse(redisUtils.hasKey("k1"));
    }

    // ========== increment / decrement ==========

    @Test
    void increment_byOne() {
        when(valueOps.increment("k1")).thenReturn(2L);
        assertEquals(2L, redisUtils.increment("k1"));
    }

    @Test
    void increment_byDelta() {
        redisUtils.increment("k1", 5);
        verify(valueOps).increment("k1", 5L);
    }

    @Test
    void decrement_byOne() {
        when(valueOps.decrement("k1")).thenReturn(0L);
        assertEquals(0L, redisUtils.decrement("k1"));
    }

    // ========== setIfAbsent ==========

    @Test
    void setIfAbsent_keyNotExists_returnsTrue() {
        when(valueOps.setIfAbsent(eq("k1"), eq("v1"), eq(60L), eq(TimeUnit.SECONDS))).thenReturn(true);
        assertTrue(redisUtils.setIfAbsent("k1", "v1", 60, TimeUnit.SECONDS));
    }

    @Test
    void setIfAbsent_keyExists_returnsFalse() {
        when(valueOps.setIfAbsent(eq("k1"), eq("v1"), eq(60L), eq(TimeUnit.SECONDS))).thenReturn(false);
        assertFalse(redisUtils.setIfAbsent("k1", "v1", 60, TimeUnit.SECONDS));
    }

    // ========== getExpire ==========

    @Test
    void getExpire_returnsTtl() {
        when(redisTemplate.getExpire("k1")).thenReturn(300L);
        assertEquals(300L, redisUtils.getExpire("k1"));
    }

    // ========== sorted set operations ==========

    @Test
    void zAdd_addsMember() {
        when(zSetOps.add("hot", "1", 1.0)).thenReturn(true);
        assertTrue(redisUtils.zAdd("hot", "1", 1.0));
    }

    @Test
    void zIncrementScore_increments() {
        when(zSetOps.incrementScore("hot", "1", 1.0)).thenReturn(5.0);
        assertEquals(5.0, redisUtils.zIncrementScore("hot", "1", 1.0));
    }

    @Test
    void zReverseRange_returnsTopMembers() {
        when(zSetOps.reverseRange("hot", 0, 9)).thenReturn(java.util.Set.of("3", "1", "2"));
        var result = redisUtils.zReverseRange("hot", 0, 9);
        assertEquals(3, result.size());
        assertTrue(result.contains("3"));
    }

    @Test
    void zRemove_removesMember() {
        when(zSetOps.remove("hot", "1")).thenReturn(1L);
        assertEquals(1L, redisUtils.zRemove("hot", "1"));
    }
}
