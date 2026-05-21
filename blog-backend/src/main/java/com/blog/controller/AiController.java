package com.blog.controller;

import com.blog.annotation.RateLimit;
import com.blog.ai.rag.DocumentIndexingService;
import com.blog.common.Result;
import com.blog.common.UserContext;
import com.blog.dto.AiRagRequest;
import com.blog.dto.AiSummaryRequest;
import com.blog.dto.AiWriteRequest;
import com.blog.dto.ChatMessage;
import com.blog.dto.ChatRequest;
import com.blog.entity.Article;
import com.blog.mapper.ArticleMapper;
import com.blog.service.AiService;
import com.blog.service.StreamCallback;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Api(tags = "AI智能助理模块")
@RestController
@RequestMapping("/api/ai")
public class AiController {
    @Autowired(required = false)
    private AiService aiService;
    private final ArticleMapper articleMapper;
    @Autowired(required = false)
    private DocumentIndexingService documentIndexingService;
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    public AiController(ArticleMapper articleMapper) {
        this.articleMapper = articleMapper;
    }

    @ApiOperation("AI对话（SSE流式输出）")
    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @RateLimit(time = 60, count = 20, message = "AI请求过于频繁，请稍后再试")
    public SseEmitter chat(@RequestBody ChatRequest request, HttpServletRequest httpRequest) {
        final String sessionId = (request.getSessionId() == null || request.getSessionId().isEmpty())
                ? UUID.randomUUID().toString()
                : request.getSessionId();

        Long userId = UserContext.getCurrentUserIdOrNull(httpRequest);
        final Long finalUserId = userId;

        SseEmitter emitter = new SseEmitter(120000L);

        // 限流检查已移至 AiServiceImpl.chatWithRag() 中通过原子操作完成
        executorService.execute(() -> {
            try {
                aiService.chatStream(sessionId, finalUserId, request.getMessage(), new StreamCallback() {
                    @Override
                    public void onStart() {
                        try {
                            emitter.send(SseEmitter.event()
                                    .name("start")
                                    .data(Map.of("sessionId", sessionId)));
                        } catch (Exception e) {
                            log.error("发送开始事件失败", e);
                        }
                    }

                    @Override
                    public void onMessage(String message) {
                        try {
                            emitter.send(SseEmitter.event()
                                    .name("message")
                                    .data(Map.of("content", message)));
                        } catch (Exception e) {
                            log.error("发送消息事件失败", e);
                        }
                    }

                    @Override
                    public void onComplete() {
                        try {
                            emitter.send(SseEmitter.event()
                                    .name("complete")
                                    .data(Map.of("sessionId", sessionId)));
                        } catch (Exception e) {
                            log.debug("发送完成事件失败(可能已关闭)", e.getMessage());
                        }
                        try {
                            emitter.complete();
                        } catch (Exception e) {
                            // emitter 可能已被 onError 或超时关闭
                        }
                    }

                    @Override
                    public void onError(Throwable t) {
                        try {
                            emitter.send(SseEmitter.event()
                                    .name("error")
                                    .data(Map.of("error", t.getMessage())));
                        } catch (Exception e) {
                            log.debug("发送错误事件失败(可能已关闭)", e.getMessage());
                        }
                        try {
                            emitter.complete();
                        } catch (Exception e) {
                            // emitter 可能已被关闭
                        }
                    }
                });
            } catch (Exception e) {
                log.error("AI对话处理异常", e);
                emitter.completeWithError(e);
            }
        });

        emitter.onTimeout(() -> {
            log.warn("SSE连接超时");
            emitter.complete();
        });

        emitter.onError(e -> log.error("SSE连接错误", e));

        return emitter;
    }

    @ApiOperation("AI写作（SSE流式输出）")
    @PostMapping(value = "/write", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @RateLimit(time = 60, count = 20, message = "AI写作请求过于频繁，请稍后再试")
    public SseEmitter write(@RequestBody AiWriteRequest request, HttpServletRequest httpRequest) {
        Long userId = UserContext.getCurrentUserIdOrNull(httpRequest);
        final Long finalUserId = userId;

        SseEmitter emitter = new SseEmitter(120000L);

        // 限流检查已移至 Service 层通过原子操作完成
        executorService.execute(() -> {
            try {
                aiService.writeStream(request.getPrompt(), new StreamCallback() {
                    @Override
                    public void onStart() {
                        try {
                            emitter.send(SseEmitter.event()
                                    .name("start")
                                    .data(Map.of("sessionId", request.getSessionId() != null ? request.getSessionId() : "")));
                        } catch (Exception e) {
                            log.error("发送开始事件失败", e);
                        }
                    }

                    @Override
                    public void onMessage(String message) {
                        try {
                            emitter.send(SseEmitter.event()
                                    .name("message")
                                    .data(Map.of("content", message)));
                        } catch (Exception e) {
                            log.error("发送消息事件失败", e);
                        }
                    }

                    @Override
                    public void onComplete() {
                        try {
                            emitter.send(SseEmitter.event()
                                    .name("complete")
                                    .data(Map.of("content", "")));
                        } catch (Exception e) {
                            log.debug("发送完成事件失败(可能已关闭)", e.getMessage());
                        }
                        try {
                            emitter.complete();
                        } catch (Exception e) {
                            // emitter 可能已被关闭
                        }
                    }

                    @Override
                    public void onError(Throwable t) {
                        try {
                            emitter.send(SseEmitter.event()
                                    .name("error")
                                    .data(Map.of("error", t.getMessage())));
                        } catch (Exception e) {
                            log.debug("发送错误事件失败(可能已关闭)", e.getMessage());
                        }
                        try {
                            emitter.complete();
                        } catch (Exception e) {
                            // emitter 可能已被关闭
                        }
                    }
                });
            } catch (Exception e) {
                log.error("AI写作处理异常", e);
                emitter.completeWithError(e);
            }
        });

        emitter.onTimeout(() -> {
            log.warn("AI写作SSE连接超时");
            emitter.complete();
        });

        emitter.onError(e -> log.error("AI写作SSE连接错误", e));

        return emitter;
    }

    @ApiOperation("获取对话历史")
    @GetMapping("/history/{sessionId}")
    public Result<List<ChatMessage>> getChatHistory(@PathVariable String sessionId) {
        List<ChatMessage> history = aiService.getChatHistory(sessionId);
        return Result.success(history);
    }

    @ApiOperation("清除对话历史")
    @DeleteMapping("/history/{sessionId}")
    public Result<Void> clearChatHistory(@PathVariable String sessionId) {
        aiService.clearChatHistory(sessionId);
        return Result.success();
    }

    @ApiOperation("RAG增强AI对话（基于博客文章内容）")
    @PostMapping(value = "/chat/rag", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @RateLimit(time = 60, count = 20, message = "AI请求过于频繁，请稍后再试")
    public SseEmitter chatWithRag(@RequestBody AiRagRequest request, HttpServletRequest httpRequest) {
        final String sessionId = (request.getSessionId() == null || request.getSessionId().isEmpty())
                ? UUID.randomUUID().toString()
                : request.getSessionId();

        Long userId = UserContext.getCurrentUserIdOrNull(httpRequest);
        final Long finalUserId = userId;

        SseEmitter emitter = new SseEmitter(120000L);

        executorService.execute(() -> {
            try {
                aiService.chatWithRag(sessionId, finalUserId, request.getMessage(), new StreamCallback() {
                    @Override
                    public void onStart() {
                        try {
                            emitter.send(SseEmitter.event()
                                    .name("start")
                                    .data(Map.of("sessionId", sessionId)));
                        } catch (Exception e) {
                            log.error("发送开始事件失败", e);
                        }
                    }

                    @Override
                    public void onMessage(String message) {
                        try {
                            emitter.send(SseEmitter.event()
                                    .name("message")
                                    .data(Map.of("content", message)));
                        } catch (Exception e) {
                            log.error("发送消息事件失败", e);
                        }
                    }

                    @Override
                    public void onComplete() {
                        try {
                            emitter.send(SseEmitter.event()
                                    .name("complete")
                                    .data(Map.of("sessionId", sessionId)));
                        } catch (Exception e) {
                            log.debug("发送完成事件失败(可能已关闭)", e.getMessage());
                        }
                        try {
                            emitter.complete();
                        } catch (Exception e) {
                            // emitter 可能已被 onError 或超时关闭
                        }
                    }

                    @Override
                    public void onError(Throwable t) {
                        try {
                            emitter.send(SseEmitter.event()
                                    .name("error")
                                    .data(Map.of("error", t.getMessage())));
                        } catch (Exception e) {
                            log.debug("发送错误事件失败(可能已关闭)", e.getMessage());
                        }
                        try {
                            emitter.complete();
                        } catch (Exception e) {
                            // emitter 可能已被关闭
                        }
                    }
                });
            } catch (Exception e) {
                log.error("RAG对话处理异常", e);
                emitter.completeWithError(e);
            }
        });

        emitter.onTimeout(() -> {
            log.warn("SSE连接超时");
            emitter.complete();
        });

        emitter.onError(e -> log.error("SSE连接错误", e));

        return emitter;
    }

    @ApiOperation("AI生成文章摘要")
    @PostMapping("/summary")
    public Result<String> summarize(@Valid @RequestBody AiSummaryRequest request) {
        Article article = articleMapper.selectById(request.getArticleId());
        if (article == null) {
            return Result.error(Result.NOT_FOUND, "文章不存在");
        }
        String summary = aiService.generateSummary(article.getContent());
        return Result.success(summary);
    }

    @ApiOperation("重新索引所有文章到向量存储")
    @PostMapping("/reindex")
    public Result<String> reindex() {
        documentIndexingService.onStartup();
        return Result.success("文章索引已更新");
    }
}
