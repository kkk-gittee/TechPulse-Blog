package com.blog.service;

import com.blog.dto.ArticleVO;

import java.util.List;

public interface HotArticleService {
    void recordView(Long articleId);

    List<ArticleVO> getHotArticles(Integer limit, Long currentUserId);

    void clearHotArticles();
}
