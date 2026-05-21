package com.blog.service;

import com.blog.mapper.ArticleMapper;
import com.blog.utils.RedisUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * 文章计数同步服务。
 *
 * 点赞数、浏览数先写入 Redis，通过定时任务批量同步到 MySQL，
 * 减轻数据库压力，同时保证最终一致性。
 *
 * Redis Key 设计：
 *   article:like:{articleId}     → String，当前点赞数增量
 *   article:view:{articleId}     → String，浏览数增量
 *   article:liked:{userId}       → Set，该用户点赞的文章 ID 集合
 *   article:dirty                 → Set，待同步的文章 ID 集合
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CounterSyncService {

    private final RedisUtils redisUtils;
    private final ArticleMapper articleMapper;

    private static final String LIKE_KEY_PREFIX = "article:like:";
    private static final String VIEW_KEY_PREFIX = "article:view:";
    private static final String DIRTY_SET_KEY = "article:dirty";

    /**
     * 增加点赞计数（Redis 增量）。
     */
    public void incrementLike(Long articleId) {
        String key = LIKE_KEY_PREFIX + articleId;
        redisUtils.increment(key, 1);
        redisUtils.set(DIRTY_SET_KEY + ":" + articleId, "1", 3600, java.util.concurrent.TimeUnit.SECONDS);
    }

    /**
     * 减少点赞计数（Redis 减量）。
     */
    public void decrementLike(Long articleId) {
        String key = LIKE_KEY_PREFIX + articleId;
        redisUtils.decrement(key);
        redisUtils.set(DIRTY_SET_KEY + ":" + articleId, "1", 3600, java.util.concurrent.TimeUnit.SECONDS);
    }

    /**
     * 增加浏览计数（Redis 增量）。
     */
    public void incrementView(Long articleId) {
        String key = VIEW_KEY_PREFIX + articleId;
        redisUtils.increment(key, 1);
        redisUtils.set(DIRTY_SET_KEY + ":" + articleId, "1", 3600, java.util.concurrent.TimeUnit.SECONDS);
    }

    /**
     * 获取文章当前点赞数（DB 值 + Redis 增量）。
     */
    public int getLikeCount(Long articleId) {
        String key = LIKE_KEY_PREFIX + articleId;
        Object delta = redisUtils.get(key);
        return delta instanceof Number ? ((Number) delta).intValue() : 0;
    }

    /**
     * 获取文章当前浏览数增量。
     */
    public int getViewDelta(Long articleId) {
        String key = VIEW_KEY_PREFIX + articleId;
        Object delta = redisUtils.get(key);
        return delta instanceof Number ? ((Number) delta).intValue() : 0;
    }

    /**
     * 定时任务：每 5 分钟将 Redis 计数增量同步到 MySQL。
     */
    @Scheduled(fixedRate = 300_000)
    public void syncToDatabase() {
        log.debug("开始同步文章计数到数据库...");
        Set<String> dirtyKeys = redisUtils.scan(DIRTY_SET_KEY + ":*");
        if (dirtyKeys == null || dirtyKeys.isEmpty()) {
            log.debug("无脏数据需要同步");
            return;
        }

        int synced = 0;
        for (String dirtyKey : dirtyKeys) {
            try {
                String articleIdStr = dirtyKey.substring(dirtyKey.lastIndexOf(':') + 1);
                Long articleId = Long.parseLong(articleIdStr);
                syncArticleCounters(articleId);
                synced++;
            } catch (NumberFormatException e) {
                log.warn("无效的脏数据 key: {}", dirtyKey);
                redisUtils.delete(dirtyKey);
            }
        }
        log.debug("文章计数同步完成，共同步 {} 篇文章", synced);
    }

    /**
     * 手动触发特定文章的计数同步（读文章详情时调用）。
     */
    public void syncArticleCounters(Long articleId) {
        String likeKey = LIKE_KEY_PREFIX + articleId;
        String viewKey = VIEW_KEY_PREFIX + articleId;

        Object likeDelta = redisUtils.get(likeKey);
        Object viewDelta = redisUtils.get(viewKey);

        if (likeDelta == null && viewDelta == null) return;

        int likeD = likeDelta instanceof Number ? ((Number) likeDelta).intValue() : 0;
        int viewD = viewDelta instanceof Number ? ((Number) viewDelta).intValue() : 0;

        if (likeD != 0 || viewD != 0) {
            // 更新数据库
            articleMapper.updateCounters(articleId, likeD, viewD);
            // 清除 Redis 增量
            redisUtils.delete(likeKey);
            redisUtils.delete(viewKey);
            redisUtils.delete(DIRTY_SET_KEY + ":" + articleId);
        }
    }
}
