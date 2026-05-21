package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.dto.ArticleDTO;
import com.blog.dto.ArticleVO;
import com.blog.entity.Article;
import com.blog.entity.Category;
import com.blog.entity.User;
import com.blog.mapper.ArticleMapper;
import com.blog.mapper.ArticleTagMapper;
import com.blog.mapper.CategoryMapper;
import com.blog.mapper.TagMapper;
import com.blog.mapper.UserMapper;
import com.blog.entity.ArticleTag;
import com.blog.entity.Tag;
import com.blog.service.ArticleService;
import com.blog.service.CounterSyncService;
import com.blog.utils.RedisUtils;
import com.blog.common.ArticleConverter;
import com.blog.common.BusinessException;
import com.blog.common.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ArticleServiceImpl implements ArticleService {
    private final ArticleMapper articleMapper;
    private final UserMapper userMapper;
    private final CategoryMapper categoryMapper;
    private final TagMapper tagMapper;
    private final ArticleTagMapper articleTagMapper;
    private final RedisUtils redisUtils;
    private final CounterSyncService counterSyncService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createArticle(Long userId, ArticleDTO articleDTO) {
        Article article = new Article();
        article.setUserId(userId);
        article.setTitle(articleDTO.getTitle());
        article.setContent(articleDTO.getContent());
        article.setSummary(articleDTO.getSummary());
        article.setCoverImage(articleDTO.getCoverImage());
        article.setCategoryId(articleDTO.getCategoryId());
        article.setTags(articleDTO.getTags());
        article.setViewCount(0);
        article.setLikeCount(0);
        article.setCommentCount(0);
        article.setStatus(1);
        article.setDeleted(0);
        articleMapper.insert(article);
        syncArticleTags(article.getId(), articleDTO.getTags());
    }

    @Override
    public ArticleVO getArticleDetail(Long articleId, Long currentUserId) {
        // 优先从缓存获取
        String cacheKey = "article:detail:" + articleId;
        ArticleVO cached = (ArticleVO) redisUtils.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        Article article = articleMapper.selectById(articleId);
        if (article == null) {
            throw new BusinessException(Result.NOT_FOUND, "文章不存在");
        }

        // 增加浏览量（通过 CounterSyncService 异步同步到 DB）
        counterSyncService.incrementView(articleId);
        article.setViewCount(article.getViewCount() + 1);

        // 合并 Redis 点赞增量到显示计数
        int likeDelta = counterSyncService.getLikeCount(articleId);
        int currentLikes = article.getLikeCount() != null ? article.getLikeCount() : 0;
        article.setLikeCount(currentLikes + likeDelta);

        Map<Long, User> singleUserMap = new HashMap<>();
        Map<Long, Category> singleCategoryMap = new HashMap<>();
        if (article.getUserId() != null) {
            User u = userMapper.selectById(article.getUserId());
            if (u != null) singleUserMap.put(u.getId(), u);
        }
        if (article.getCategoryId() != null) {
            Category c = categoryMapper.selectById(article.getCategoryId());
            if (c != null) singleCategoryMap.put(c.getId(), c);
        }
        ArticleVO vo = ArticleConverter.toVO(article, singleUserMap, singleCategoryMap);
        redisUtils.set(cacheKey, vo, 30, TimeUnit.MINUTES);
        return vo;
    }

    @Override
    public Page<ArticleVO> getArticleList(Integer pageNum, Integer pageSize, Long currentUserId) {
        Page<Article> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Article::getStatus, 1).orderByDesc(Article::getCreateTime);
        Page<Article> articlePage = articleMapper.selectPage(page, wrapper);

        // Batch fetch users and categories
        Set<Long> userIds = articlePage.getRecords().stream().map(Article::getUserId).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<Long> categoryIds = articlePage.getRecords().stream().map(Article::getCategoryId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, User> userMap = userIds.isEmpty() ? new HashMap<>() : userMapper.selectBatchIds(userIds).stream().collect(Collectors.toMap(User::getId, u -> u));
        Map<Long, Category> categoryMap = categoryIds.isEmpty() ? new HashMap<>() : categoryMapper.selectBatchIds(categoryIds).stream().collect(Collectors.toMap(Category::getId, c -> c));

        Page<ArticleVO> voPage = new Page<>(pageNum, pageSize, articlePage.getTotal());
        List<ArticleVO> voList = new ArrayList<>();
        for (Article article : articlePage.getRecords()) {
            voList.add(ArticleConverter.toVO(article, userMap, categoryMap));
        }
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateArticle(Long userId, Long articleId, ArticleDTO articleDTO) {
        Article article = articleMapper.selectById(articleId);
        if (article == null) {
            throw new BusinessException(Result.NOT_FOUND, "文章不存在");
        }
        if (!Objects.equals(article.getUserId(), userId)) {
            throw new BusinessException(Result.FORBIDDEN, "无权修改此文章");
        }

        article.setTitle(articleDTO.getTitle());
        article.setContent(articleDTO.getContent());
        article.setSummary(articleDTO.getSummary());
        article.setCoverImage(articleDTO.getCoverImage());
        article.setCategoryId(articleDTO.getCategoryId());
        article.setTags(articleDTO.getTags());
        articleMapper.updateById(article);
        syncArticleTags(articleId, articleDTO.getTags());

        // 清除缓存
        redisUtils.delete("article:detail:" + articleId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteArticle(Long userId, Long articleId) {
        Article article = articleMapper.selectById(articleId);
        if (article == null) {
            throw new BusinessException(Result.NOT_FOUND, "文章不存在");
        }
        if (!Objects.equals(article.getUserId(), userId)) {
            throw new BusinessException(Result.FORBIDDEN, "无权删除此文章");
        }
        articleMapper.deleteById(articleId);
        redisUtils.delete("article:detail:" + articleId);
    }

    @Override
    public Page<ArticleVO> getUserArticles(Long userId, Integer pageNum, Integer pageSize, Long currentUserId) {
        Page<Article> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Article::getUserId, userId).eq(Article::getStatus, 1).orderByDesc(Article::getCreateTime);
        Page<Article> articlePage = articleMapper.selectPage(page, wrapper);

        // Batch fetch users and categories
        Set<Long> userIds = articlePage.getRecords().stream().map(Article::getUserId).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<Long> categoryIds = articlePage.getRecords().stream().map(Article::getCategoryId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, User> userMap = userIds.isEmpty() ? new HashMap<>() : userMapper.selectBatchIds(userIds).stream().collect(Collectors.toMap(User::getId, u -> u));
        Map<Long, Category> categoryMap = categoryIds.isEmpty() ? new HashMap<>() : categoryMapper.selectBatchIds(categoryIds).stream().collect(Collectors.toMap(Category::getId, c -> c));

        Page<ArticleVO> voPage = new Page<>(pageNum, pageSize, articlePage.getTotal());
        List<ArticleVO> voList = new ArrayList<>();
        for (Article article : articlePage.getRecords()) {
            voList.add(ArticleConverter.toVO(article, userMap, categoryMap));
        }
        voPage.setRecords(voList);
        return voPage;
    }

    /**
     * 同步文章标签：解析 CSV 标签字符串，写入 article_tag 关联表。
     */
    private void syncArticleTags(Long articleId, String tagsCsv) {
        // 删除旧关联
        LambdaQueryWrapper<ArticleTag> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(ArticleTag::getArticleId, articleId);
        articleTagMapper.delete(deleteWrapper);

        if (tagsCsv == null || tagsCsv.trim().isEmpty()) {
            return;
        }

        String[] tagNames = tagsCsv.split(",");
        for (String tagName : tagNames) {
            String name = tagName.trim();
            if (name.isEmpty()) continue;

            // 查找或创建标签
            Tag tag = tagMapper.selectByName(name);
            if (tag == null) {
                tag = new Tag();
                tag.setName(name);
                tag.setUseCount(0);
                tagMapper.insert(tag);
            }

            // 插入关联
            ArticleTag articleTag = new ArticleTag();
            articleTag.setArticleId(articleId);
            articleTag.setTagId(tag.getId());
            articleTagMapper.insert(articleTag);

            // 增加使用计数（原子操作）
            tagMapper.incrementUseCount(tag.getId());
        }
    }

}
