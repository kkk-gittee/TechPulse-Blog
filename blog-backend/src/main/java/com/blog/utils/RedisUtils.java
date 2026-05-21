package com.blog.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class RedisUtils {

    private static final int MAX_MEMORY_CACHE_SIZE = 10000;
    private final Map<String, Object> memoryCache = new ConcurrentHashMap<>();
    private final Map<String, Long> expireMap = new ConcurrentHashMap<>();

    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    private boolean isRedisAvailable() {
        try {
            return redisTemplate != null && redisTemplate.getConnectionFactory() != null;
        } catch (Exception e) {
            return false;
        }
    }

    public void set(String key, Object value, long timeout, TimeUnit unit) {
        if (isRedisAvailable()) {
            try {
                if (unit != null && timeout > 0) {
                    redisTemplate.opsForValue().set(key, value, timeout, unit);
                } else {
                    redisTemplate.opsForValue().set(key, value);
                }
                return;
            } catch (Exception e) {
                log.warn("Redis set失败，使用内存缓存: {}", e.getMessage());
            }
        }
        if (memoryCache.size() >= MAX_MEMORY_CACHE_SIZE) {
            log.warn("内存缓存已满({}), 跳过写入", MAX_MEMORY_CACHE_SIZE);
            return;
        }
        memoryCache.put(key, value);
        if (unit != null && timeout > 0) {
            expireMap.put(key, System.currentTimeMillis() + unit.toMillis(timeout));
        }
    }

    public Object get(String key) {
        if (isRedisAvailable()) {
            try {
                return redisTemplate.opsForValue().get(key);
            } catch (Exception e) {
                log.warn("Redis get失败，使用内存缓存: {}", e.getMessage());
            }
        }
        Long expire = expireMap.get(key);
        if (expire != null && System.currentTimeMillis() > expire) {
            memoryCache.remove(key);
            expireMap.remove(key);
            return null;
        }
        return memoryCache.get(key);
    }

    public Boolean delete(String key) {
        if (isRedisAvailable()) {
            try {
                return redisTemplate.delete(key);
            } catch (Exception e) {
                log.warn("Redis delete失败: {}", e.getMessage());
            }
        }
        memoryCache.remove(key);
        expireMap.remove(key);
        return true;
    }

    public Boolean hasKey(String key) {
        if (isRedisAvailable()) {
            try {
                return redisTemplate.hasKey(key);
            } catch (Exception e) {
                log.warn("Redis hasKey失败: {}", e.getMessage());
            }
        }
        Long expire = expireMap.get(key);
        if (expire != null && System.currentTimeMillis() > expire) {
            memoryCache.remove(key);
            expireMap.remove(key);
            return false;
        }
        return memoryCache.containsKey(key);
    }

    public Long increment(String key) {
        if (isRedisAvailable()) {
            try {
                return redisTemplate.opsForValue().increment(key);
            } catch (Exception e) {
                log.warn("Redis increment失败: {}", e.getMessage());
            }
        }
        Long expire = expireMap.get(key);
        if (expire != null && System.currentTimeMillis() > expire) {
            memoryCache.remove(key);
            expireMap.remove(key);
        }
        Object value = memoryCache.get(key);
        long newValue = (value instanceof Number) ? ((Number) value).longValue() + 1 : 1;
        memoryCache.put(key, newValue);
        return newValue;
    }

    public Long decrement(String key) {
        if (isRedisAvailable()) {
            try {
                return redisTemplate.opsForValue().decrement(key);
            } catch (Exception e) {
                log.warn("Redis decrement失败: {}", e.getMessage());
            }
        }
        Object value = memoryCache.get(key);
        long newValue = (value instanceof Number) ? ((Number) value).longValue() - 1 : -1;
        memoryCache.put(key, newValue);
        return newValue;
    }

    public void increment(String key, long delta) {
        if (isRedisAvailable()) {
            try {
                redisTemplate.opsForValue().increment(key, delta);
                return;
            } catch (Exception e) {
                log.warn("Redis increment失败: {}", e.getMessage());
            }
        }
        Object value = memoryCache.get(key);
        long newValue = (value instanceof Number) ? ((Number) value).longValue() + delta : delta;
        memoryCache.put(key, newValue);
    }

    public Boolean setIfAbsent(String key, Object value, long timeout, TimeUnit unit) {
        if (isRedisAvailable()) {
            try {
                return redisTemplate.opsForValue().setIfAbsent(key, value, timeout, unit);
            } catch (Exception e) {
                log.warn("Redis setIfAbsent失败，使用内存缓存: {}", e.getMessage());
            }
        }
        // 内存缓存实现SETNX语义
        synchronized (memoryCache) {
            Long expire = expireMap.get(key);
            if (expire != null && System.currentTimeMillis() > expire) {
                memoryCache.remove(key);
                expireMap.remove(key);
            }
            if (memoryCache.containsKey(key)) {
                return false;
            }
            memoryCache.put(key, value);
            if (unit != null && timeout > 0) {
                expireMap.put(key, System.currentTimeMillis() + unit.toMillis(timeout));
            }
            return true;
        }
    }

    public Long getExpire(String key) {
        if (isRedisAvailable()) {
            try {
                return redisTemplate.getExpire(key);
            } catch (Exception e) {
                log.warn("Redis getExpire失败: {}", e.getMessage());
            }
        }
        Long expire = expireMap.get(key);
        if (expire == null) return -1L;
        long remaining = expire - System.currentTimeMillis();
        return remaining > 0 ? remaining / 1000 : -2L;
    }

    /**
     * 原子操作：递增 key 并设置过期时间（仅在首次递增时设置 TTL）。
     * 用于限流计数器，解决 increment + set TTL 的竞态条件。
     */
    public long incrementWithExpire(String key, long expireSeconds) {
        if (isRedisAvailable()) {
            try {
                DefaultRedisScript<Long> script = new DefaultRedisScript<>(
                        "local current = redis.call('INCR', KEYS[1])\n" +
                        "if current == 1 then\n" +
                        "    redis.call('EXPIRE', KEYS[1], ARGV[1])\n" +
                        "end\n" +
                        "return current", Long.class);
                return redisTemplate.execute(script, Collections.singletonList(key), expireSeconds);
            } catch (Exception e) {
                log.warn("Redis incrementWithExpire失败: {}", e.getMessage());
            }
        }
        // 本地降级
        return increment(key);
    }

    /**
     * 原子操作：切换 Set 元素（存在则删除返回 1，不存在则添加返回 0）。
     * 用于点赞/评论点赞，解决 isLiked + toggle 的竞态条件。
     */
    public long toggleSetMember(String setKey, String member) {
        if (isRedisAvailable()) {
            try {
                DefaultRedisScript<Long> script = new DefaultRedisScript<>(
                        "if redis.call('SISMEMBER', KEYS[1], ARGV[1]) == 1 then\n" +
                        "    redis.call('SREM', KEYS[1], ARGV[1])\n" +
                        "    return 1\n" +
                        "else\n" +
                        "    redis.call('SADD', KEYS[1], ARGV[1])\n" +
                        "    return 0\n" +
                        "end", Long.class);
                return redisTemplate.execute(script, Collections.singletonList(setKey), member);
            } catch (Exception e) {
                log.warn("Redis toggleSetMember失败: {}", e.getMessage());
            }
        }
        // 本地降级
        String userKey = setKey + ":user:" + member;
        if (hasKey(userKey)) {
            delete(userKey);
            decrement(setKey);
            return 1;
        } else {
            set(userKey, 1, 0, null);
            increment(setKey);
            return 0;
        }
    }

    /**
     * 根据 pattern 匹配 key 集合。
     * 注意：生产环境应使用 SCAN 替代 KEYS，避免阻塞 Redis。
     */
    public Set<String> keys(String pattern) {
        if (isRedisAvailable()) {
            try {
                return redisTemplate.keys(pattern);
            } catch (Exception e) {
                log.warn("Redis keys失败: {}", e.getMessage());
            }
        }
        return Collections.emptySet();
    }

    /**
     * 使用 SCAN 游标遍历匹配的 key，避免 KEYS 命令阻塞 Redis。
     */
    public Set<String> scan(String pattern) {
        if (isRedisAvailable()) {
            try {
                return redisTemplate.execute((RedisCallback<Set<String>>) connection -> {
                    ScanOptions options = ScanOptions.scanOptions().match(pattern).count(100).build();
                    Set<String> keys = new java.util.HashSet<>();
                    try (var cursor = connection.scan(options)) {
                        while (cursor.hasNext()) {
                            keys.add(new String(cursor.next(), java.nio.charset.StandardCharsets.UTF_8));
                        }
                    }
                    return keys;
                });
            } catch (Exception e) {
                log.warn("Redis scan失败: {}", e.getMessage());
            }
        }
        return Collections.emptySet();
    }

    /**
     * 检查 Set 中是否包含指定成员。
     */
    public Boolean isMember(String key, String member) {
        if (isRedisAvailable()) {
            try {
                return redisTemplate.opsForSet().isMember(key, member);
            } catch (Exception e) {
                log.warn("Redis isMember失败: {}", e.getMessage());
            }
        }
        return hasKey(key + ":user:" + member);
    }

    /**
     * 获取 Set 的所有成员。
     */
    public Set<Object> members(String key) {
        if (isRedisAvailable()) {
            try {
                return redisTemplate.opsForSet().members(key);
            } catch (Exception e) {
                log.warn("Redis members失败: {}", e.getMessage());
            }
        }
        return Collections.emptySet();
    }

    /**
     * 向 Sorted Set 添加元素或增加分数。
     */
    public Boolean zAdd(String key, String member, double score) {
        if (isRedisAvailable()) {
            try {
                return redisTemplate.opsForZSet().add(key, member, score);
            } catch (Exception e) {
                log.warn("Redis zAdd失败: {}", e.getMessage());
            }
        }
        return false;
    }

    /**
     * 给 Sorted Set 元素增加分数（原子操作）。
     */
    public Double zIncrementScore(String key, String member, double delta) {
        if (isRedisAvailable()) {
            try {
                return redisTemplate.opsForZSet().incrementScore(key, member, delta);
            } catch (Exception e) {
                log.warn("Redis zIncrementScore失败: {}", e.getMessage());
            }
        }
        return null;
    }

    /**
     * 按分数从高到低获取 Sorted Set 的 top N 成员。
     */
    public Set<String> zReverseRange(String key, long start, long end) {
        if (isRedisAvailable()) {
            try {
                Set<Object> members = redisTemplate.opsForZSet().reverseRange(key, start, end);
                Set<String> result = new java.util.HashSet<>();
                if (members != null) {
                    for (Object m : members) {
                        result.add(m.toString());
                    }
                }
                return result;
            } catch (Exception e) {
                log.warn("Redis zReverseRange失败: {}", e.getMessage());
            }
        }
        return Collections.emptySet();
    }

    /**
     * 从 Sorted Set 中移除指定元素。
     */
    public Long zRemove(String key, String... members) {
        if (isRedisAvailable()) {
            try {
                return redisTemplate.opsForZSet().remove(key, (Object[]) members);
            } catch (Exception e) {
                log.warn("Redis zRemove失败: {}", e.getMessage());
            }
        }
        return 0L;
    }
}
