package com.blog.service.impl;

import com.blog.ai.assistant.StreamingBlogAssistant;
import com.blog.ai.tools.BlogQueryTool;
import com.blog.config.AiConfig;
import com.blog.dto.ChatMessage;
import com.blog.service.AiService;
import com.blog.service.StreamCallback;
import com.blog.utils.RedisUtils;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.dashscope.QwenChatModel;
import dev.langchain4j.model.dashscope.QwenStreamingChatModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.TokenStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Slf4j
@Service
@ConditionalOnBean(QwenStreamingChatModel.class)
public class AiServiceImpl implements AiService {

    private final QwenStreamingChatModel streamingModel;
    private final QwenChatModel chatModel;
    private final ContentRetriever contentRetriever;
    private final BlogQueryTool blogQueryTool;
    private final AiConfig aiConfig;
    private final RedisUtils redisUtils;

    private static final String DAILY_LIMIT_KEY = "ai:daily:limit:";

    private final Map<String, dev.langchain4j.memory.ChatMemory> memoryMap =
            Collections.synchronizedMap(new LinkedHashMap<>(100, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, dev.langchain4j.memory.ChatMemory> eldest) {
                    return size() > 100;
                }
            });

    public AiServiceImpl(QwenStreamingChatModel streamingModel,
                         QwenChatModel chatModel,
                         ContentRetriever contentRetriever,
                         BlogQueryTool blogQueryTool,
                         AiConfig aiConfig,
                         RedisUtils redisUtils) {
        this.streamingModel = streamingModel;
        this.chatModel = chatModel;
        this.contentRetriever = contentRetriever;
        this.blogQueryTool = blogQueryTool;
        this.aiConfig = aiConfig;
        this.redisUtils = redisUtils;
    }

    private dev.langchain4j.memory.ChatMemory getOrCreateMemory(String sessionId) {
        return memoryMap.computeIfAbsent(sessionId,
                id -> dev.langchain4j.memory.chat.MessageWindowChatMemory.withMaxMessages(aiConfig.getMaxHistory()));
    }

    private StreamingBlogAssistant buildAssistant() {
        return AiServices.builder(StreamingBlogAssistant.class)
                .streamingChatLanguageModel(streamingModel)
                .chatMemoryProvider(id -> getOrCreateMemory(id.toString()))
                .contentRetriever(contentRetriever)
                .tools(blogQueryTool)
                .build();
    }

    @Override
    public void chatStream(String sessionId, Long userId, String message, StreamCallback callback) {
        chatWithRag(sessionId, userId, message, callback);
    }

    @Override
    public void chatWithRag(String sessionId, Long userId, String message, StreamCallback callback) {
        try {
            // 原子操作：先递增再检查，避免 check+increment 竞态条件
            if (userId != null) {
                long currentCount = redisUtils.incrementWithExpire(DAILY_LIMIT_KEY + userId, 86400);
                if (currentCount > aiConfig.getDailyLimit()) {
                    // 超限，回滚计数
                    redisUtils.increment(DAILY_LIMIT_KEY + userId, -1);
                    callback.onError(new RuntimeException("今日AI调用次数已达上限"));
                    return;
                }
            }

            callback.onStart();

            StreamingBlogAssistant assistant = buildAssistant();
            TokenStream tokenStream = assistant.chat(sessionId, message);

            tokenStream
                    .onNext(callback::onMessage)
                    .onComplete(response -> callback.onComplete())
                    .onError(callback::onError)
                    .start();

        } catch (Exception e) {
            log.error("AI对话异常", e);
            callback.onError(e);
        }
    }

    @Override
    public void writeStream(String prompt, StreamCallback callback) {
        try {
            callback.onStart();

            dev.langchain4j.memory.ChatMemory memory = dev.langchain4j.memory.chat.MessageWindowChatMemory.withMaxMessages(2);

            StreamingBlogAssistant assistant = AiServices.builder(StreamingBlogAssistant.class)
                    .streamingChatLanguageModel(streamingModel)
                    .chatMemoryProvider(id -> memory)
                    .build();

            TokenStream tokenStream = assistant.chat("write", prompt);
            tokenStream
                    .onNext(callback::onMessage)
                    .onComplete(response -> callback.onComplete())
                    .onError(callback::onError)
                    .start();

        } catch (Exception e) {
            log.error("AI写作异常", e);
            callback.onError(e);
        }
    }

    @Override
    public String generateSummary(String articleContent) {
        String prompt = "请为以下文章生成一段简洁的摘要（不超过200字），突出核心观点和技术要点：\n\n" + articleContent;
        List<dev.langchain4j.data.message.ChatMessage> messages = List.of(UserMessage.from(prompt));
        return chatModel.generate(messages).content().text();
    }

    @Override
    public List<ChatMessage> getChatHistory(String sessionId) {
        dev.langchain4j.memory.ChatMemory memory = memoryMap.get(sessionId);
        if (memory == null) {
            return new ArrayList<>();
        }
        return memory.messages().stream()
                .filter(m -> m instanceof UserMessage || m instanceof AiMessage)
                .map(m -> {
                    if (m instanceof UserMessage) {
                        return ChatMessage.user(((UserMessage) m).singleText());
                    } else {
                        return ChatMessage.assistant(((AiMessage) m).text());
                    }
                })
                .collect(Collectors.toList());
    }

    @Override
    public void clearChatHistory(String sessionId) {
        memoryMap.remove(sessionId);
    }
}
