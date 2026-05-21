package com.blog.service.impl;

import com.blog.common.ArticleConverter;
import com.blog.dto.ArticleVO;
import com.blog.entity.Article;
import com.blog.entity.Category;
import com.blog.entity.User;
import com.blog.mapper.ArticleMapper;
import com.blog.mapper.CategoryMapper;
import com.blog.mapper.UserMapper;
import com.blog.service.HotArticleService;
import com.blog.utils.RedisUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HotArticleServiceImpl implements HotArticleService {
    private final RedisUtils redisUtils;
    private final ArticleMapper articleMapper;
    private final UserMapper userMapper;
    private final CategoryMapper categoryMapper;

    private static final String HOT_ARTICLE_KEY = "hot:articles";

    @Override
    public void recordView(Long articleId) {
        redisUtils.zIncrementScore(HOT_ARTICLE_KEY, String.valueOf(articleId), 1);
    }

    @Override
    public List<ArticleVO> getHotArticles(Integer limit, Long currentUserId) {
        Set<String> topMembers = redisUtils.zReverseRange(HOT_ARTICLE_KEY, 0, limit - 1);
        if (topMembers.isEmpty()) return new ArrayList<>();

        List<Long> articleIds = topMembers.stream()
                .map(Long::parseLong)
                .collect(Collectors.toList());

        // Batch fetch articles
        List<Article> articles = articleMapper.selectBatchIds(articleIds);
        Map<Long, Article> articleMap = articles.stream()
                .filter(a -> a.getStatus() == 1)
                .collect(Collectors.toMap(Article::getId, a -> a));

        // Batch fetch users and categories
        Set<Long> userIds = articles.stream().map(Article::getUserId).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<Long> categoryIds = articles.stream().map(Article::getCategoryId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, User> userMap = userIds.isEmpty() ? new HashMap<>() : userMapper.selectBatchIds(userIds).stream().collect(Collectors.toMap(User::getId, u -> u));
        Map<Long, Category> categoryMap = categoryIds.isEmpty() ? new HashMap<>() : categoryMapper.selectBatchIds(categoryIds).stream().collect(Collectors.toMap(Category::getId, c -> c));

        // Assemble VOs preserving hot order
        List<ArticleVO> result = new ArrayList<>();
        for (Long articleId : articleIds) {
            Article article = articleMap.get(articleId);
            if (article == null) continue;
            result.add(ArticleConverter.toVO(article, userMap, categoryMap));
        }
        return result;
    }

    @Override
    public void clearHotArticles() {
        redisUtils.delete(HOT_ARTICLE_KEY);
    }
}
