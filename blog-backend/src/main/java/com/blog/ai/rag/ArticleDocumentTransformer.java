package com.blog.ai.rag;

import com.blog.entity.Article;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.Metadata;
import org.springframework.stereotype.Component;

@Component
public class ArticleDocumentTransformer {

    public Document toDocument(Article article) {
        String content = "标题: " + article.getTitle() + "\n\n" + article.getContent();
        Metadata metadata = Metadata.from("articleId", String.valueOf(article.getId()))
                .put("title", article.getTitle())
                .put("categoryId", String.valueOf(article.getCategoryId()))
                .put("tags", article.getTags() != null ? article.getTags() : "");
        return Document.from(content, metadata);
    }
}
