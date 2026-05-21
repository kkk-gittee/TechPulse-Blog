package com.blog.service;

public interface LikeService {
    Boolean toggleLike(Long userId, Long articleId);

    Boolean isLiked(Long userId, Long articleId);
}
