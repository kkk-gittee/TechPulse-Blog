package com.blog.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationMessage {
    private Long targetUserId;
    private Long fromUserId;
    private String fromUsername;
    private String fromNickname;
    private String type;
    private Long referenceId;
    private String content;
    private String createdAt;
}
