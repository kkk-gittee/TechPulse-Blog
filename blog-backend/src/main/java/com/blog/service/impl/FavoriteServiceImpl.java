package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.dto.ArticleVO;
import com.blog.entity.Article;
import com.blog.entity.Category;
import com.blog.entity.Favorite;
import com.blog.entity.User;
import com.blog.mapper.ArticleMapper;
import com.blog.mapper.CategoryMapper;
import com.blog.mapper.FavoriteMapper;
import com.blog.mapper.UserMapper;
import com.blog.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl implements FavoriteService {
    private final FavoriteMapper favoriteMapper;
    private final ArticleMapper articleMapper;
    private final UserMapper userMapper;
    private final CategoryMapper categoryMapper;

    @Override
    public Boolean toggleFavorite(Long userId, Long articleId) {
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId).eq(Favorite::getArticleId, articleId);
        Favorite favorite = favoriteMapper.selectOne(wrapper);

        if (favorite != null) {
            favoriteMapper.deleteById(favorite.getId());
            return false;
        } else {
            Favorite newFavorite = new Favorite();
            newFavorite.setUserId(userId);
            newFavorite.setArticleId(articleId);
            favoriteMapper.insert(newFavorite);
            return true;
        }
    }

    @Override
    public Boolean isFavorited(Long userId, Long articleId) {
        if (userId == null) return false;
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId).eq(Favorite::getArticleId, articleId);
        return favoriteMapper.selectCount(wrapper) > 0;
    }

    @Override
    public List<Long> getUserFavoriteArticleIds(Long userId) {
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId);
        return favoriteMapper.selectList(wrapper).stream()
                .map(Favorite::getArticleId)
                .collect(Collectors.toList());
    }

    @Override
    public List<ArticleVO> getUserFavoriteArticles(Long userId) {
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId).orderByDesc(Favorite::getCreateTime);
        List<Favorite> favorites = favoriteMapper.selectList(wrapper);
        if (favorites.isEmpty()) return new ArrayList<>();

        // Batch fetch articles
        List<Long> articleIds = favorites.stream().map(Favorite::getArticleId).collect(Collectors.toList());
        List<Article> articles = articleMapper.selectBatchIds(articleIds);
        Map<Long, Article> articleMap = articles.stream()
                .filter(a -> a.getStatus() == 1)
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

        // Assemble VOs preserving favorite order
        List<ArticleVO> voList = new ArrayList<>();
        for (Favorite fav : favorites) {
            Article article = articleMap.get(fav.getArticleId());
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
            vo.setIsFavorited(true);

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
        return voList;
    }
}
