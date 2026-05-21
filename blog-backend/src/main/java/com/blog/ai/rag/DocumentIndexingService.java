package com.blog.ai.rag;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.ai.event.ArticleDeletedEvent;
import com.blog.ai.event.ArticleSavedEvent;
import com.blog.entity.Article;
import com.blog.mapper.ArticleMapper;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnBean(EmbeddingStoreIngestor.class)
public class DocumentIndexingService {

    private final ArticleMapper articleMapper;
    private final ArticleDocumentTransformer transformer;
    private final EmbeddingStoreIngestor ingestor;

    @EventListener(ApplicationReadyEvent.class)
    public void onStartup() {
        List<Article> articles = articleMapper.selectList(
                new LambdaQueryWrapper<Article>().eq(Article::getStatus, 1)
        );
        if (articles.isEmpty()) {
            log.info("没有已发布的文章，跳过向量索引");
            return;
        }
        List<Document> docs = articles.stream()
                .map(transformer::toDocument)
                .collect(Collectors.toList());
        ingestor.ingest(docs);
        log.info("已索引 {} 篇文章到向量存储", docs.size());
    }

    @EventListener
    public void onArticleSaved(ArticleSavedEvent event) {
        Article article = articleMapper.selectById(event.getArticleId());
        if (article != null && article.getStatus() == 1) {
            Document doc = transformer.toDocument(article);
            ingestor.ingest(doc);
            log.info("已索引文章: id={}, title={}", article.getId(), article.getTitle());
        }
    }

    @EventListener
    public void onArticleDeleted(ArticleDeletedEvent event) {
        log.info("文章已删除，向量将在下次全量重建时更新: id={}", event.getArticleId());
    }
}
