package com.blog.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.dto.ArticleDTO;
import com.blog.dto.ArticleVO;

public interface ArticleService {
    void createArticle(Long userId, ArticleDTO articleDTO);

    ArticleVO getArticleDetail(Long articleId, Long currentUserId);

    Page<ArticleVO> getArticleList(Integer pageNum, Integer pageSize, Long currentUserId);

    void updateArticle(Long userId, Long articleId, ArticleDTO articleDTO);

    void deleteArticle(Long userId, Long articleId);

    Page<ArticleVO> getUserArticles(Long userId, Integer pageNum, Integer pageSize, Long currentUserId);
}
