package com.blog.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class AiRagRequest {
    @NotBlank(message = "消息不能为空")
    private String message;

    private String sessionId;
}
