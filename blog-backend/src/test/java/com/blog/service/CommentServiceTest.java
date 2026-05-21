package com.blog.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.dto.CommentDTO;
import com.blog.dto.CommentVO;
import com.blog.entity.Article;
import com.blog.entity.Comment;
import com.blog.entity.User;
import com.blog.mapper.ArticleMapper;
import com.blog.mapper.CommentMapper;
import com.blog.mapper.UserMapper;
import com.blog.service.impl.CommentServiceImpl;
import com.blog.utils.RedisUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentMapper commentMapper;
    @Mock
    private UserMapper userMapper;
    @Mock
    private ArticleMapper articleMapper;
    @Mock
    private NotificationService notificationService;
    @Mock
    private RedisUtils redisUtils;
    @InjectMocks
    private CommentServiceImpl commentService;

    // ========== addComment ==========

    @Test
    void addComment_success() {
        Article article = new Article();
        article.setId(1L);
        article.setCommentCount(5);
        article.setUserId(10L);

        when(articleMapper.selectById(1L)).thenReturn(article);

        CommentDTO dto = new CommentDTO();
        dto.setArticleId(1L);
        dto.setContent("Great article!");

        commentService.addComment(2L, dto);

        verify(commentMapper).insert(argThat(c ->
                Long.valueOf(1L).equals(c.getArticleId()) &&
                Long.valueOf(2L).equals(c.getUserId()) &&
                "Great article!".equals(c.getContent()) &&
                Integer.valueOf(0).equals(c.getLikeCount())
        ));
        verify(articleMapper).incrementCommentCount(1L, 1);
        verify(notificationService).sendCommentNotification(10L, 2L, 1L, "Great article!");
    }

    @Test
    void addComment_replyNotification() {
        Article article = new Article();
        article.setId(1L);
        article.setCommentCount(3);
        article.setUserId(10L);

        when(articleMapper.selectById(1L)).thenReturn(article);

        CommentDTO dto = new CommentDTO();
        dto.setArticleId(1L);
        dto.setReplyUserId(5L);
        dto.setContent("Reply content");

        commentService.addComment(2L, dto);

        verify(notificationService).sendCommentNotification(5L, 2L, 1L, "Reply content");
    }

    @Test
    void addComment_articleNotFound_throws() {
        when(articleMapper.selectById(999L)).thenReturn(null);

        CommentDTO dto = new CommentDTO();
        dto.setArticleId(999L);
        dto.setContent("X");

        assertThrows(RuntimeException.class, () -> commentService.addComment(1L, dto));
    }

    // ========== getArticleComments ==========

    @Test
    void getArticleComments_empty() {
        when(commentMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());

        List<CommentVO> result = commentService.getArticleComments(1L, null);

        assertTrue(result.isEmpty());
    }

    @Test
    void getArticleComments_withReplies() {
        Comment root = new Comment();
        root.setId(1L);
        root.setArticleId(1L);
        root.setUserId(1L);
        root.setParentId(null);

        User user = new User();
        user.setId(1L);
        user.setUsername("author");

        when(commentMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(root))   // root comments
                .thenReturn(Collections.emptyList()); // replies

        when(userMapper.selectBatchIds(anyCollection())).thenReturn(List.of(user));

        List<CommentVO> result = commentService.getArticleComments(1L, null);

        assertEquals(1, result.size());
        assertEquals("author", result.get(0).getUsername());
    }

    // ========== deleteComment ==========

    @Test
    void deleteComment_success() {
        Comment comment = new Comment();
        comment.setId(1L);
        comment.setUserId(1L);
        comment.setArticleId(10L);

        Article article = new Article();
        article.setId(10L);
        article.setCommentCount(5);

        when(commentMapper.selectById(1L)).thenReturn(comment);
        when(articleMapper.selectById(10L)).thenReturn(article);

        commentService.deleteComment(1L, 1L);

        verify(commentMapper).deleteById(1L);
        verify(articleMapper).updateById(argThat(a ->
                Integer.valueOf(4).equals(a.getCommentCount())));
    }

    @Test
    void deleteComment_notFound_throws() {
        when(commentMapper.selectById(999L)).thenReturn(null);

        assertThrows(RuntimeException.class, () -> commentService.deleteComment(1L, 999L));
    }

    @Test
    void deleteComment_notOwner_throws() {
        Comment comment = new Comment();
        comment.setId(1L);
        comment.setUserId(1L);

        when(commentMapper.selectById(1L)).thenReturn(comment);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> commentService.deleteComment(2L, 1L));
        assertEquals("无权删除此评论", ex.getMessage());
    }

    // ========== toggleLikeComment ==========

    @Test
    void toggleLikeComment_like() {
        when(redisUtils.toggleSetMember("comment_like:1", "2")).thenReturn(0L);

        Comment comment = new Comment();
        comment.setId(1L);
        comment.setLikeCount(0);
        when(commentMapper.selectById(1L)).thenReturn(comment);

        Boolean result = commentService.toggleLikeComment(2L, 1L);

        assertTrue(result);
        verify(redisUtils).toggleSetMember("comment_like:1", "2");
    }

    @Test
    void toggleLikeComment_unlike() {
        when(redisUtils.toggleSetMember("comment_like:1", "2")).thenReturn(1L);

        Comment comment = new Comment();
        comment.setId(1L);
        comment.setLikeCount(3);
        when(commentMapper.selectById(1L)).thenReturn(comment);

        Boolean result = commentService.toggleLikeComment(2L, 1L);

        assertFalse(result);
        verify(redisUtils).toggleSetMember("comment_like:1", "2");
    }
}
