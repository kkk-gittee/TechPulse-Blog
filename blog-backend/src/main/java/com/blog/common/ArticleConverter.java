package com.blog.common;

import com.blog.dto.ArticleVO;
import com.blog.entity.Article;
import com.blog.entity.Category;
import com.blog.entity.User;

import java.util.Map;

public class ArticleConverter {

    public static ArticleVO toVO(Article article, Map<Long, User> userMap, Map<Long, Category> categoryMap) {
        ArticleVO vo = new ArticleVO();
        vo.setId(article.getId());
        vo.setUserId(article.getUserId());
        vo.setTitle(article.getTitle());
        vo.setSummary(article.getSummary());
        vo.setCoverImage(article.getCoverImage());
        vo.setViewCount(article.getViewCount());
        vo.setLikeCount(article.getLikeCount());
        vo.setCommentCount(article.getCommentCount());
        vo.setStatus(article.getStatus());
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
            Category category = categoryMap.get(article.getCategoryId());
            if (category != null) {
                vo.setCategoryName(category.getName());
            }
        }

        return vo;
    }
}
