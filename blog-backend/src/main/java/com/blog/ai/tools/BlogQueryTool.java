package com.blog.ai.tools;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.entity.Article;
import com.blog.entity.Category;
import com.blog.mapper.ArticleMapper;
import com.blog.mapper.CategoryMapper;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class BlogQueryTool {

    private final ArticleMapper articleMapper;
    private final CategoryMapper categoryMapper;

    @Tool("搜索博客文章，根据关键词查找相关文章，返回标题、摘要和ID列表")
    public String searchArticles(String keyword) {
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Article::getStatus, 1)
                .and(w -> w.like(Article::getTitle, keyword)
                        .or().like(Article::getContent, keyword))
                .orderByDesc(Article::getViewCount)
                .last("LIMIT 5");
        List<Article> articles = articleMapper.selectList(wrapper);
        if (articles.isEmpty()) {
            return "未找到与\"" + keyword + "\"相关的文章";
        }
        return articles.stream()
                .map(a -> String.format("[%d] %s — %s (浏览:%d)",
                        a.getId(), a.getTitle(),
                        a.getSummary() != null ? a.getSummary().substring(0, Math.min(50, a.getSummary().length())) : "无摘要",
                        a.getViewCount()))
                .collect(Collectors.joining("\n"));
    }

    @Tool("获取文章详情，根据文章ID返回完整内容")
    public String getArticleDetail(Long articleId) {
        Article article = articleMapper.selectById(articleId);
        if (article == null) {
            return "文章不存在";
        }
        return String.format("标题: %s\n分类ID: %d\n标签: %s\n\n%s",
                article.getTitle(), article.getCategoryId(),
                article.getTags() != null ? article.getTags() : "无",
                article.getContent());
    }

    @Tool("获取所有文章分类列表")
    public String getAllCategories() {
        List<Category> categories = categoryMapper.selectList(null);
        return categories.stream()
                .map(c -> String.format("[%d] %s", c.getId(), c.getName()))
                .collect(Collectors.joining("\n"));
    }

    @Tool("获取热门文章列表（按浏览量排序前5篇）")
    public String getPopularArticles() {
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Article::getStatus, 1)
                .orderByDesc(Article::getViewCount)
                .last("LIMIT 5");
        List<Article> articles = articleMapper.selectList(wrapper);
        return articles.stream()
                .map(a -> String.format("[%d] %s (浏览:%d, 点赞:%d)",
                        a.getId(), a.getTitle(), a.getViewCount(), a.getLikeCount()))
                .collect(Collectors.joining("\n"));
    }
}
