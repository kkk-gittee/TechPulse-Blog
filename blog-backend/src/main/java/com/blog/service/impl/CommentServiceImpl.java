package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.dto.CommentDTO;
import com.blog.dto.CommentVO;
import com.blog.entity.Article;
import com.blog.entity.Comment;
import com.blog.entity.User;
import com.blog.mapper.ArticleMapper;
import com.blog.mapper.CommentMapper;
import com.blog.mapper.UserMapper;
import com.blog.service.CommentService;
import com.blog.service.NotificationService;
import com.blog.utils.RedisUtils;
import com.blog.common.BusinessException;
import com.blog.common.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {
    private final CommentMapper commentMapper;
    private final UserMapper userMapper;
    private final ArticleMapper articleMapper;
    private final NotificationService notificationService;
    private final RedisUtils redisUtils;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addComment(Long userId, CommentDTO commentDTO) {
        Article article = articleMapper.selectById(commentDTO.getArticleId());
        if (article == null) {
            throw new BusinessException(Result.NOT_FOUND, "文章不存在");
        }

        Comment comment = new Comment();
        comment.setArticleId(commentDTO.getArticleId());
        comment.setUserId(userId);
        comment.setParentId(commentDTO.getParentId());
        comment.setReplyUserId(commentDTO.getReplyUserId());
        comment.setContent(commentDTO.getContent());
        comment.setLikeCount(0);
        comment.setStatus(1);
        comment.setDeleted(0);
        commentMapper.insert(comment);

        // 更新文章评论数（原子操作，避免竞态条件）
        articleMapper.incrementCommentCount(commentDTO.getArticleId(), 1);

        // 发送评论通知
        if (commentDTO.getReplyUserId() != null) {
            notificationService.sendCommentNotification(commentDTO.getReplyUserId(), userId, commentDTO.getArticleId(), commentDTO.getContent());
        } else {
            notificationService.sendCommentNotification(article.getUserId(), userId, commentDTO.getArticleId(), commentDTO.getContent());
        }
    }

    @Override
    public List<CommentVO> getArticleComments(Long articleId, Long userId) {
        // 1. 查询所有根评论
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getArticleId, articleId)
                .eq(Comment::getParentId, null)
                .orderByDesc(Comment::getCreateTime);
        List<Comment> rootComments = commentMapper.selectList(wrapper);

        if (rootComments.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. 批量查询所有子评论（消除 N+1）
        List<Long> rootIds = rootComments.stream().map(Comment::getId).collect(Collectors.toList());
        LambdaQueryWrapper<Comment> replyWrapper = new LambdaQueryWrapper<>();
        replyWrapper.in(Comment::getParentId, rootIds).orderByAsc(Comment::getCreateTime);
        List<Comment> allReplies = commentMapper.selectList(replyWrapper);
        Map<Long, List<Comment>> repliesByParent = allReplies.stream()
                .collect(Collectors.groupingBy(Comment::getParentId));

        // 3. 批量查询所有相关用户（消除 N+1）
        Set<Long> allUserIds = new HashSet<>();
        rootComments.forEach(c -> allUserIds.add(c.getUserId()));
        allReplies.forEach(r -> {
            allUserIds.add(r.getUserId());
            if (r.getReplyUserId() != null) allUserIds.add(r.getReplyUserId());
        });
        Map<Long, User> userMap = allUserIds.isEmpty() ? Collections.emptyMap()
                : userMapper.selectBatchIds(allUserIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        // 4. 批量查询点赞状态
        Map<Long, Boolean> likeStatusMap = new HashMap<>();
        if (userId != null) {
            rootComments.forEach(c -> likeStatusMap.put(c.getId(), isCommentLiked(userId, c.getId())));
            allReplies.forEach(r -> likeStatusMap.put(r.getId(), isCommentLiked(userId, r.getId())));
        }

        // 5. 组装结果
        return rootComments.stream().map(root -> {
            CommentVO vo = toVO(root, userMap, likeStatusMap);
            List<Comment> replies = repliesByParent.getOrDefault(root.getId(), Collections.emptyList());
            vo.setReplies(replies.stream()
                    .map(reply -> toVO(reply, userMap, likeStatusMap))
                    .collect(Collectors.toList()));
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteComment(Long userId, Long commentId) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new BusinessException(Result.NOT_FOUND, "评论不存在");
        }
        if (!Objects.equals(comment.getUserId(), userId)) {
            throw new BusinessException(Result.FORBIDDEN, "无权删除此评论");
        }
        commentMapper.deleteById(commentId);

        Article article = articleMapper.selectById(comment.getArticleId());
        if (article != null) {
            article.setCommentCount(Math.max(0, article.getCommentCount() - 1));
            articleMapper.updateById(article);
        }
    }

    @Override
    public Boolean toggleLikeComment(Long userId, Long commentId) {
        String setKey = "comment_like:" + commentId;
        String member = String.valueOf(userId);

        // 原子操作：存在则删除返回 1（已点赞→取消），不存在则添加返回 0（未点赞→点赞）
        long wasLiked = redisUtils.toggleSetMember(setKey, member);

        if (wasLiked == 1) {
            updateCommentLikeCount(commentId, -1);
            return false;
        } else {
            updateCommentLikeCount(commentId, 1);
            return true;
        }
    }

    @Override
    public Boolean isCommentLiked(Long userId, Long commentId) {
        String setKey = "comment_like:" + commentId;
        return redisUtils.isMember(setKey, String.valueOf(userId));
    }

    private void updateCommentLikeCount(Long commentId, int delta) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment != null) {
            comment.setLikeCount(Math.max(0, comment.getLikeCount() + delta));
            commentMapper.updateById(comment);
        }
    }

    private CommentVO toVO(Comment comment, Map<Long, User> userMap, Map<Long, Boolean> likeStatusMap) {
        CommentVO vo = new CommentVO();
        vo.setId(comment.getId());
        vo.setArticleId(comment.getArticleId());
        vo.setUserId(comment.getUserId());
        vo.setParentId(comment.getParentId());
        vo.setReplyUserId(comment.getReplyUserId());
        vo.setContent(comment.getContent());
        vo.setLikeCount(comment.getLikeCount());
        vo.setCreateTime(comment.getCreateTime());

        User user = userMap.get(comment.getUserId());
        if (user != null) {
            vo.setUsername(user.getUsername());
            vo.setNickname(user.getNickname());
            vo.setAvatar(user.getAvatar());
        }

        if (comment.getReplyUserId() != null) {
            User replyUser = userMap.get(comment.getReplyUserId());
            if (replyUser != null) {
                vo.setReplyUsername(replyUser.getUsername());
                vo.setReplyNickname(replyUser.getNickname());
            }
        }

        vo.setIsLiked(likeStatusMap.getOrDefault(comment.getId(), false));
        return vo;
    }
}
