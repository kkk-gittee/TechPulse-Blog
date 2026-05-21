package com.blog.config;

import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.dashscope.QwenChatModel;
import dev.langchain4j.model.dashscope.QwenEmbeddingModel;
import dev.langchain4j.model.dashscope.QwenStreamingChatModel;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnExpression("!T(org.springframework.util.StringUtils).isEmpty('${ai.api-key:}')")
public class LangChainConfig {

    @Bean
    public QwenStreamingChatModel qwenStreamingChatModel(AiConfig aiConfig) {
        return QwenStreamingChatModel.builder()
                .apiKey(aiConfig.getApiKey())
                .modelName(aiConfig.getModel())
                .build();
    }

    @Bean
    public QwenChatModel qwenChatModel(AiConfig aiConfig) {
        return QwenChatModel.builder()
                .apiKey(aiConfig.getApiKey())
                .modelName(aiConfig.getModel())
                .build();
    }

    @Bean
    public QwenEmbeddingModel qwenEmbeddingModel(AiConfig aiConfig) {
        return QwenEmbeddingModel.builder()
                .apiKey(aiConfig.getApiKey())
                .modelName(aiConfig.getEmbeddingModelName())
                .build();
    }

    @Bean
    public InMemoryEmbeddingStore<TextSegment> embeddingStore() {
        java.io.File file = new java.io.File("embedding-store.db");
        if (file.exists()) {
            return InMemoryEmbeddingStore.fromFile(file.toPath());
        }
        return new InMemoryEmbeddingStore<>();
    }

    @Bean
    public EmbeddingStoreIngestor embeddingStoreIngestor(
            InMemoryEmbeddingStore<TextSegment> store,
            QwenEmbeddingModel embeddingModel,
            AiConfig aiConfig) {
        return EmbeddingStoreIngestor.builder()
                .embeddingStore(store)
                .embeddingModel(embeddingModel)
                .documentSplitter(DocumentSplitters.recursive(
                        aiConfig.getRag().getChunkSize(),
                        aiConfig.getRag().getChunkOverlap()))
                .build();
    }

    @Bean
    public EmbeddingStoreContentRetriever contentRetriever(
            InMemoryEmbeddingStore<TextSegment> store,
            QwenEmbeddingModel embeddingModel,
            AiConfig aiConfig) {
        return EmbeddingStoreContentRetriever.builder()
                .embeddingStore(store)
                .embeddingModel(embeddingModel)
                .maxResults(aiConfig.getRag().getMaxResults())
                .minScore(aiConfig.getRag().getMinScore())
                .build();
    }
}
