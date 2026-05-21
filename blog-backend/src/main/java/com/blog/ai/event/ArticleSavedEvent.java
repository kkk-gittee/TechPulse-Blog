package com.blog.ai.event;

import lombok.Getter;

@Getter
public class ArticleSavedEvent {
    private final Long articleId;

    public ArticleSavedEvent(Long articleId) {
        this.articleId = articleId;
    }
}
