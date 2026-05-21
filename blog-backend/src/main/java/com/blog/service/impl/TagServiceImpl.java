package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.entity.ArticleTag;
import com.blog.entity.Tag;
import com.blog.mapper.ArticleTagMapper;
import com.blog.mapper.TagMapper;
import com.blog.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TagServiceImpl implements TagService {
    private final TagMapper tagMapper;
    private final ArticleTagMapper articleTagMapper;

    @Override
    public List<Tag> listHotTags(Integer limit) {
        LambdaQueryWrapper<Tag> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Tag::getUseCount).last("LIMIT " + limit);
        return tagMapper.selectList(wrapper);
    }

    @Override
    public List<Tag> listAllTags() {
        return tagMapper.selectList(null);
    }

    @Override
    @Transactional
    public void addTagsToArticle(Long articleId, List<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty()) {
            return;
        }

        for (String tagName : tagNames) {
            if (tagName.trim().isEmpty()) continue;

            // 查找或创建标签
            LambdaQueryWrapper<Tag> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Tag::getName, tagName.trim());
            Tag tag = tagMapper.selectOne(wrapper);

            if (tag == null) {
                tag = new Tag();
                tag.setName(tagName.trim());
                tag.setUseCount(1);
                tagMapper.insert(tag);
            } else {
                tag.setUseCount(tag.getUseCount() + 1);
                tagMapper.updateById(tag);
            }

            // 创建文章-标签关联
            ArticleTag articleTag = new ArticleTag();
            articleTag.setArticleId(articleId);
            articleTag.setTagId(tag.getId());
            articleTagMapper.insert(articleTag);
        }
    }
}
