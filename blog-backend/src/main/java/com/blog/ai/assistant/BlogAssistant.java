package com.blog.ai.assistant;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

public interface BlogAssistant {

    @SystemMessage("""
            你是"技术脉动"博客的 AI 智能助理。请基于提供的博客文章内容回答用户问题。
            如果上下文中没有相关信息，请如实告知。
            """)
    String chat(@MemoryId String sessionId, @UserMessage String message);
}
