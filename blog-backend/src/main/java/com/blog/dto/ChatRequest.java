package com.blog.dto;

import lombok.Data;

@Data
public class ChatRequest {
    private String message;
    private String sessionId;
}
