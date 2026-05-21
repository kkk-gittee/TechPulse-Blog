package com.blog.dto;

import lombok.Data;

@Data
public class AiWriteRequest {
    private String prompt;
    private String mode;      // generate, optimize, complete
    private String title;
    private String sessionId;
}
