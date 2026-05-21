package com.blog.service;

import com.blog.dto.NotificationMessage;

public interface NotificationService {
    void sendLikeNotification(Long targetUserId, Long fromUserId, Long articleId);

    void sendCommentNotification(Long targetUserId, Long fromUserId, Long articleId, String commentContent);

    void sendFollowNotification(Long targetUserId, Long fromUserId);
}
