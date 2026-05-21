package com.blog.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.dto.ArticleVO;
import com.blog.dto.SearchDTO;

public interface SearchService {
    Page<ArticleVO> searchArticles(SearchDTO searchDTO, Long currentUserId);
}
