package com.blog.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.dto.ArticleVO;

public interface BrowseHistoryService {
    void addHistory(Long userId, Long articleId);

    Page<ArticleVO> getUserHistory(Long userId, Integer pageNum, Integer pageSize);

    void clearHistory(Long userId);
}
