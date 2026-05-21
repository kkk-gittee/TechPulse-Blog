package com.blog.service.impl;

import com.blog.entity.Article;
import com.blog.mapper.ArticleMapper;
import com.blog.service.CounterSyncService;
import com.blog.service.LikeService;
import com.blog.service.NotificationService;
import com.blog.utils.RedisUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LikeServiceImpl implements LikeService {
    private final RedisUtils redisUtils;
    private final ArticleMapper articleMapper;
    private final NotificationService notificationService;
    private final CounterSyncService counterSyncService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean toggleLike(Long userId, Long articleId) {
        String setKey = "like:" + articleId;
        String member = String.valueOf(userId);

        // 原子操作：存在则删除返回 1（已点赞→取消），不存在则添加返回 0（未点赞→点赞）
        long wasLiked = redisUtils.toggleSetMember(setKey, member);

        if (wasLiked == 1) {
            counterSyncService.decrementLike(articleId);
            return false;
        } else {
            counterSyncService.incrementLike(articleId);

            // 发送点赞通知
            Article article = articleMapper.selectById(articleId);
            if (article != null) {
                notificationService.sendLikeNotification(article.getUserId(), userId, articleId);
            }

            return true;
        }
    }

    @Override
    public Boolean isLiked(Long userId, Long articleId) {
        String setKey = "like:" + articleId;
        return redisUtils.isMember(setKey, String.valueOf(userId));
    }
}
