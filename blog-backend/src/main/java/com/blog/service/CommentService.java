package com.blog.service;

import com.blog.dto.CommentDTO;
import com.blog.dto.CommentVO;

import java.util.List;

public interface CommentService {
    void addComment(Long userId, CommentDTO commentDTO);

    List<CommentVO> getArticleComments(Long articleId, Long userId);

    void deleteComment(Long userId, Long commentId);

    Boolean toggleLikeComment(Long userId, Long commentId);

    Boolean isCommentLiked(Long userId, Long commentId);
}
