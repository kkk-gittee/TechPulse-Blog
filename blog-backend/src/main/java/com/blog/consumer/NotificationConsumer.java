package com.blog.consumer;

import com.blog.dto.NotificationMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(name = "rabbitmq.enabled", havingValue = "true", matchIfMissing = true)
public class NotificationConsumer {

    @Autowired(required = false)
    private SimpMessagingTemplate messagingTemplate;

    @RabbitListener(queues = "notification.like")
    public void handleLikeNotification(NotificationMessage message) {
        log.info("收到点赞通知: {}", message);
        sendToWebSocket(message);
    }

    @RabbitListener(queues = "notification.comment")
    public void handleCommentNotification(NotificationMessage message) {
        log.info("收到评论通知: {}", message);
        sendToWebSocket(message);
    }

    @RabbitListener(queues = "notification.follow")
    public void handleFollowNotification(NotificationMessage message) {
        log.info("收到关注通知: {}", message);
        sendToWebSocket(message);
    }

    private void sendToWebSocket(NotificationMessage message) {
        if (messagingTemplate != null) {
            try {
                messagingTemplate.convertAndSendToUser(
                        message.getTargetUserId().toString(),
                        "/queue/notifications",
                        message
                );
            } catch (Exception e) {
                log.warn("WebSocket发送通知失败: {}", e.getMessage());
            }
        }
    }
}
