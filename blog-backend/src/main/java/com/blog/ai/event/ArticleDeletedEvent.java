package com.blog.ai.event;

import lombok.Getter;

@Getter
public class ArticleDeletedEvent {
    private final Long articleId;

    public ArticleDeletedEvent(Long articleId) {
        this.articleId = articleId;
    }
}
