package com.blog.service;

import com.blog.entity.Tag;

import java.util.List;

public interface TagService {
    List<Tag> listHotTags(Integer limit);

    List<Tag> listAllTags();

    void addTagsToArticle(Long articleId, List<String> tagNames);
}
