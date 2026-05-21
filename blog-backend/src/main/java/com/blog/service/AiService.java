package com.blog.service;

import com.blog.dto.ChatMessage;

import java.util.List;

public interface AiService {
    /**
     * 发送对话请求（流式）
     */
    void chatStream(String sessionId, Long userId, String message, StreamCallback callback);

    /**
     * RAG 增强对话（流式），基于博客文章内容回答
     */
    void chatWithRag(String sessionId, Long userId, String message, StreamCallback callback);

    /**
     * AI写作（流式），直接发送prompt，不附加系统提示词和对话历史
     */
    void writeStream(String prompt, StreamCallback callback);

    /**
     * AI 生成文章摘要（同步）
     */
    String generateSummary(String articleContent);

    /**
     * 获取对话历史
     */
    List<ChatMessage> getChatHistory(String sessionId);

    /**
     * 清除对话历史
     */
    void clearChatHistory(String sessionId);
}
