package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.dto.ArticleVO;
import com.blog.entity.Article;
import com.blog.entity.BrowseHistory;
import com.blog.entity.Category;
import com.blog.entity.User;
import com.blog.mapper.ArticleMapper;
import com.blog.mapper.BrowseHistoryMapper;
import com.blog.mapper.CategoryMapper;
import com.blog.mapper.UserMapper;
import com.blog.service.BrowseHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BrowseHistoryServiceImpl implements BrowseHistoryService {
    private final BrowseHistoryMapper browseHistoryMapper;
    private final ArticleMapper articleMapper;
    private final UserMapper userMapper;
    private final CategoryMapper categoryMapper;

    @Override
    public void addHistory(Long userId, Long articleId) {
        // 先删除旧记录（如果存在）
        LambdaQueryWrapper<BrowseHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BrowseHistory::getUserId, userId).eq(BrowseHistory::getArticleId, articleId);
        browseHistoryMapper.delete(wrapper);

        // 插入新记录
        BrowseHistory history = new BrowseHistory();
        history.setUserId(userId);
        history.setArticleId(articleId);
        browseHistoryMapper.insert(history);
    }

    @Override
    public Page<ArticleVO> getUserHistory(Long userId, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<BrowseHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BrowseHistory::getUserId, userId).orderByDesc(BrowseHistory::getCreateTime);

        Page<BrowseHistory> historyPage = browseHistoryMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        if (historyPage.getRecords().isEmpty()) {
            Page<ArticleVO> emptyPage = new Page<>(pageNum, pageSize, 0);
            emptyPage.setRecords(new ArrayList<>());
            return emptyPage;
        }

        // Batch fetch articles
        List<Long> articleIds = historyPage.getRecords().stream()
                .map(BrowseHistory::getArticleId).collect(Collectors.toList());
        List<Article> articles = articleMapper.selectBatchIds(articleIds);
        Map<Long, Article> articleMap = articles.stream()
                .collect(Collectors.toMap(Article::getId, a -> a));

        // Batch fetch users
        Set<Long> userIds = articles.stream().map(Article::getUserId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, User> userMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            userMap = userMapper.selectBatchIds(userIds).stream()
                    .collect(Collectors.toMap(User::getId, u -> u));
        }

        // Batch fetch categories
        Set<Long> categoryIds = articles.stream().map(Article::getCategoryId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, Category> categoryMap = new HashMap<>();
        if (!categoryIds.isEmpty()) {
            categoryMap = categoryMapper.selectBatchIds(categoryIds).stream()
                    .collect(Collectors.toMap(Category::getId, c -> c));
        }

        // Assemble VOs preserving history order
        List<ArticleVO> voList = new ArrayList<>();
        for (BrowseHistory history : historyPage.getRecords()) {
            Article article = articleMap.get(history.getArticleId());
            if (article == null) continue;
            ArticleVO vo = new ArticleVO();
            vo.setId(article.getId());
            vo.setUserId(article.getUserId());
            vo.setTitle(article.getTitle());
            vo.setSummary(article.getSummary());
            vo.setCoverImage(article.getCoverImage());
            vo.setViewCount(article.getViewCount());
            vo.setLikeCount(article.getLikeCount());
            vo.setCommentCount(article.getCommentCount());
            vo.setCategoryId(article.getCategoryId());
            vo.setTags(article.getTags());
            vo.setCreateTime(article.getCreateTime());

            User user = userMap.get(article.getUserId());
            if (user != null) {
                vo.setUsername(user.getUsername());
                vo.setNickname(user.getNickname());
                vo.setAvatar(user.getAvatar());
            }

            if (article.getCategoryId() != null) {
                Category cat = categoryMap.get(article.getCategoryId());
                if (cat != null) {
                    vo.setCategoryName(cat.getName());
                }
            }

            voList.add(vo);
        }

        Page<ArticleVO> voPage = new Page<>(pageNum, pageSize, historyPage.getTotal());
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public void clearHistory(Long userId) {
        LambdaQueryWrapper<BrowseHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BrowseHistory::getUserId, userId);
        browseHistoryMapper.delete(wrapper);
    }
}
