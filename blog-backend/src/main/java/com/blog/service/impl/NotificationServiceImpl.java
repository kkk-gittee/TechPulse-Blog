package com.blog.service.impl;

import com.blog.dto.NotificationMessage;
import com.blog.entity.User;
import com.blog.mapper.UserMapper;
import com.blog.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final UserMapper userMapper;

    @Autowired(required = false)
    private RabbitTemplate rabbitTemplate;

    @Override
    public void sendLikeNotification(Long targetUserId, Long fromUserId, Long articleId) {
        if (targetUserId.equals(fromUserId)) return;

        User fromUser = userMapper.selectById(fromUserId);
        if (fromUser == null) return;
        NotificationMessage message = new NotificationMessage(
                targetUserId,
                fromUserId,
                fromUser.getUsername(),
                fromUser.getNickname(),
                "LIKE",
                articleId,
                "赞了你的文章",
                LocalDateTime.now().format(FORMATTER)
        );

        sendMessage(message);
    }

    @Override
    public void sendCommentNotification(Long targetUserId, Long fromUserId, Long articleId, String commentContent) {
        if (targetUserId.equals(fromUserId)) return;

        User fromUser = userMapper.selectById(fromUserId);
        if (fromUser == null) return;
        NotificationMessage message = new NotificationMessage(
                targetUserId,
                fromUserId,
                fromUser.getUsername(),
                fromUser.getNickname(),
                "COMMENT",
                articleId,
                "评论了你的文章: " + commentContent,
                LocalDateTime.now().format(FORMATTER)
        );

        sendMessage(message);
    }

    @Override
    public void sendFollowNotification(Long targetUserId, Long fromUserId) {
        if (targetUserId.equals(fromUserId)) return;

        User fromUser = userMapper.selectById(fromUserId);
        if (fromUser == null) return;
        NotificationMessage message = new NotificationMessage(
                targetUserId,
                fromUserId,
                fromUser.getUsername(),
                fromUser.getNickname(),
                "FOLLOW",
                fromUserId,
                "关注了你",
                LocalDateTime.now().format(FORMATTER)
        );

        sendMessage(message);
    }

    private void sendMessage(NotificationMessage message) {
        if (rabbitTemplate != null) {
            try {
                rabbitTemplate.convertAndSend(
                        "notification.exchange",
                        "notification." + message.getType().toLowerCase(),
                        message
                );
                log.info("发送通知成功: to={}, type={}", message.getTargetUserId(), message.getType());
            } catch (Exception e) {
                log.warn("发送通知失败，RabbitMQ可能未启动: {}", e.getMessage());
            }
        } else {
            log.info("通知已记录（RabbitMQ未启用）: to={}, type={}", message.getTargetUserId(), message.getType());
        }
    }
}
