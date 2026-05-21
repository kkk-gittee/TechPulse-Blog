package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.common.ArticleConverter;
import com.blog.dto.ArticleVO;
import com.blog.dto.SearchDTO;
import com.blog.entity.Article;
import com.blog.entity.Category;
import com.blog.entity.User;
import com.blog.mapper.ArticleMapper;
import com.blog.mapper.ArticleTagMapper;
import com.blog.mapper.CategoryMapper;
import com.blog.mapper.UserMapper;
import com.blog.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {
    private final ArticleMapper articleMapper;
    private final UserMapper userMapper;
    private final CategoryMapper categoryMapper;
    private final ArticleTagMapper articleTagMapper;

    @Override
    public Page<ArticleVO> searchArticles(SearchDTO searchDTO, Long currentUserId) {
        Page<Article> page = new Page<>(searchDTO.getPageNum(), searchDTO.getPageSize());
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();

        wrapper.eq(Article::getStatus, 1);

        // 按标签过滤
        if (searchDTO.getTagName() != null && !searchDTO.getTagName().trim().isEmpty()) {
            List<Long> articleIds = articleTagMapper.selectArticleIdsByTagName(searchDTO.getTagName().trim());
            if (articleIds.isEmpty()) {
                // 没有匹配的文章，返回空结果
                Page<ArticleVO> emptyPage = new Page<>(searchDTO.getPageNum(), searchDTO.getPageSize(), 0);
                emptyPage.setRecords(new ArrayList<>());
                return emptyPage;
            }
            wrapper.in(Article::getId, articleIds);
        }

        if (searchDTO.getKeyword() != null && !searchDTO.getKeyword().trim().isEmpty()) {
            String keyword = searchDTO.getKeyword().trim();
            wrapper.and(w -> w
                    .like(Article::getTitle, keyword)
                    .or()
                    .like(Article::getContent, keyword)
            );
        }

        if (searchDTO.getCategoryId() != null) {
            wrapper.eq(Article::getCategoryId, searchDTO.getCategoryId());
        }

        wrapper.orderByDesc(Article::getCreateTime);

        Page<Article> articlePage = articleMapper.selectPage(page, wrapper);

        // Batch fetch users and categories
        Set<Long> userIds = articlePage.getRecords().stream().map(Article::getUserId).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<Long> categoryIds = articlePage.getRecords().stream().map(Article::getCategoryId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, User> userMap = userIds.isEmpty() ? new HashMap<>() : userMapper.selectBatchIds(userIds).stream().collect(Collectors.toMap(User::getId, u -> u));
        Map<Long, Category> categoryMap = categoryIds.isEmpty() ? new HashMap<>() : categoryMapper.selectBatchIds(categoryIds).stream().collect(Collectors.toMap(Category::getId, c -> c));

        Page<ArticleVO> voPage = new Page<>(searchDTO.getPageNum(), searchDTO.getPageSize(), articlePage.getTotal());
        List<ArticleVO> voList = new ArrayList<>();
        for (Article article : articlePage.getRecords()) {
            voList.add(ArticleConverter.toVO(article, userMap, categoryMap));
        }
        voPage.setRecords(voList);
        return voPage;
    }

}
