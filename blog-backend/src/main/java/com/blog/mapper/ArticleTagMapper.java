package com.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.entity.ArticleTag;
import com.blog.entity.Tag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ArticleTagMapper extends BaseMapper<ArticleTag> {
    @Select("SELECT t.id, t.name FROM tag t INNER JOIN article_tag at ON t.id = at.tag_id WHERE at.article_id = #{articleId}")
    List<Tag> selectTagsByArticleId(@Param("articleId") Long articleId);

    @Select("SELECT at.article_id FROM article_tag at INNER JOIN tag t ON at.tag_id = t.id WHERE t.name = #{tagName}")
    List<Long> selectArticleIdsByTagName(@Param("tagName") String tagName);
}
