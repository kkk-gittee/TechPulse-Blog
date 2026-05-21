package com.blog.service;

import com.blog.dto.ArticleVO;

import java.util.List;

public interface FavoriteService {
    Boolean toggleFavorite(Long userId, Long articleId);

    Boolean isFavorited(Long userId, Long articleId);

    List<Long> getUserFavoriteArticleIds(Long userId);

    List<ArticleVO> getUserFavoriteArticles(Long userId);
}
