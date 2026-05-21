package com.blog.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "ai")
public class AiConfig {
    private String apiKey;
    private String model = "qwen-turbo";
    private String embeddingModelName = "text-embedding-v2";
    private Integer maxHistory = 20;
    private Integer dailyLimit = 100;
    private String systemPrompt;
    private Rag rag = new Rag();

    @Data
    public static class Rag {
        private boolean enabled = true;
        private int maxResults = 5;
        private double minScore = 0.6;
        private int chunkSize = 500;
        private int chunkOverlap = 100;
    }
}
