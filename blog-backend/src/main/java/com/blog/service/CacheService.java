package com.blog.service;

import com.blog.utils.RedisUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Redis Cache-Aside 缓存服务。
 *
 * 实现三大防护：
 * 1. 缓存穿透（Cache Penetration）—— 空值缓存，短 TTL
 * 2. 缓存击穿（Cache Breakdown） —— 分布式互斥锁（SETNX），单线程重建
 * 3. 缓存雪崩（Cache Avalanche） —— 基础 TTL + 随机抖动
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CacheService {

    private final RedisUtils redisUtils;

    // 空值标记，防止缓存穿透
    private static final String NULL_MARKER = "__NULL__";
    // 空值缓存 TTL（秒），短于正常缓存，防止占用内存
    private static final long NULL_TTL_SECONDS = 60;

    // 互斥锁后缀
    private static final String LOCK_SUFFIX = ":lock";
    // 互斥锁 TTL（秒），防止死锁
    private static final long LOCK_TTL_SECONDS = 10;

    // 基础 TTL 范围
    public static final long TTL_SHORT = 300;       // 5 分钟
    public static final long TTL_MEDIUM = 1800;     // 30 分钟
    public static final long TTL_LONG = 7200;       // 2 小时

    /**
     * Cache-Aside 读取：缓存命中直接返回，未命中则从 DB 加载并回填。
     *
     * @param key      缓存键
     * @param ttlSeconds 缓存 TTL（秒）
     * @param loader   数据库加载函数
     * @param <T>      返回值类型
     * @return 缓存或数据库中的数据，null 表示不存在
     */
    @SuppressWarnings("unchecked")
    public <T> T getOrLoad(String key, long ttlSeconds, Supplier<T> loader) {
        // 1. 查缓存
        Object cached = redisUtils.get(key);
        if (cached != null) {
            if (NULL_MARKER.equals(cached)) {
                return null; // 命中空值缓存，直接返回
            }
            return (T) cached;
        }

        // 2. 缓存未命中 → 获取互斥锁，防止缓存击穿
        String lockKey = key + LOCK_SUFFIX;
        boolean locked = tryLock(lockKey);

        if (locked) {
            try {
                // 双重检查：获取锁后再次检查缓存
                cached = redisUtils.get(key);
                if (cached != null) {
                    if (NULL_MARKER.equals(cached)) return null;
                    return (T) cached;
                }

                // 3. 从数据库加载
                T value = loader.get();

                // 4. 回填缓存
                if (value != null) {
                    redisUtils.set(key, value, ttlSeconds + randomJitter(), TimeUnit.SECONDS);
                } else {
                    // 缓存穿透防护：空值也缓存，但 TTL 较短
                    redisUtils.set(key, NULL_MARKER, NULL_TTL_SECONDS, TimeUnit.SECONDS);
                }
                return value;
            } finally {
                unlock(lockKey);
            }
        } else {
            // 未获取锁 → 短暂等待后重试读缓存
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            cached = redisUtils.get(key);
            if (cached != null) {
                if (NULL_MARKER.equals(cached)) return null;
                return (T) cached;
            }
            // 兜底：直接查库
            T value = loader.get();
            if (value != null) {
                redisUtils.set(key, value, ttlSeconds + randomJitter(), TimeUnit.SECONDS);
            }
            return value;
        }
    }

    /**
     * 写入缓存并删除旧数据。
     */
    public void put(String key, Object value, long ttlSeconds) {
        redisUtils.set(key, value, ttlSeconds + randomJitter(), TimeUnit.SECONDS);
    }

    /**
     * 删除缓存（用于数据更新后失效缓存）。
     */
    public void evict(String key) {
        redisUtils.delete(key);
    }

    /**
     * 批量删除匹配模式的缓存。
     */
    public void evictPattern(String pattern) {
        java.util.Set<String> keys = redisUtils.scan(pattern);
        for (String key : keys) {
            redisUtils.delete(key);
        }
    }

    // ─── 互斥锁 ─────────────────────────────────────────

    private boolean tryLock(String key) {
        try {
            return redisUtils.setIfAbsent(key, "1", LOCK_TTL_SECONDS, TimeUnit.SECONDS);
        } catch (Exception e) {
            return false;
        }
    }

    private void unlock(String key) {
        try {
            redisUtils.delete(key);
        } catch (Exception e) {
            log.warn("释放锁失败: {}", key);
        }
    }

    /**
     * 随机抖动（±15%），防止缓存雪崩。
     */
    private long randomJitter() {
        return (long) (Math.random() * 0.3 * TTL_SHORT);
    }
}
