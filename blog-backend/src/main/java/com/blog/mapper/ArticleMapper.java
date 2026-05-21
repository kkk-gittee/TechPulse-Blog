package com.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.entity.Article;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface ArticleMapper extends BaseMapper<Article> {
    @Update("UPDATE article SET like_count = like_count + #{likeDelta}, view_count = view_count + #{viewDelta} WHERE id = #{articleId}")
    int updateCounters(@Param("articleId") Long articleId, @Param("likeDelta") int likeDelta, @Param("viewDelta") int viewDelta);

    @Update("UPDATE article SET comment_count = comment_count + #{delta} WHERE id = #{articleId}")
    int incrementCommentCount(@Param("articleId") Long articleId, @Param("delta") int delta);
}
