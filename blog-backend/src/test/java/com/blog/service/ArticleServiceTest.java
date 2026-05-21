package com.blog.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.dto.ArticleDTO;
import com.blog.dto.ArticleVO;
import com.blog.entity.Article;
import com.blog.entity.Category;
import com.blog.entity.User;
import com.blog.mapper.ArticleMapper;
import com.blog.mapper.ArticleTagMapper;
import com.blog.mapper.CategoryMapper;
import com.blog.mapper.TagMapper;
import com.blog.mapper.UserMapper;
import com.blog.service.impl.ArticleServiceImpl;
import com.blog.utils.RedisUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ArticleServiceTest {

    @Mock
    private ArticleMapper articleMapper;
    @Mock
    private UserMapper userMapper;
    @Mock
    private CategoryMapper categoryMapper;
    @Mock
    private TagMapper tagMapper;
    @Mock
    private ArticleTagMapper articleTagMapper;
    @Mock
    private RedisUtils redisUtils;
    @Mock
    private CounterSyncService counterSyncService;
    @InjectMocks
    private ArticleServiceImpl articleService;

    // ========== createArticle ==========

    @Test
    void createArticle_success() {
        ArticleDTO dto = new ArticleDTO();
        dto.setTitle("Test Title");
        dto.setContent("Content");
        dto.setSummary("Summary");
        dto.setCategoryId(1L);
        dto.setTags("java,spring");

        articleService.createArticle(1L, dto);

        verify(articleMapper).insert(argThat(a ->
                "Test Title".equals(a.getTitle()) &&
                Long.valueOf(1L).equals(a.getUserId()) &&
                Integer.valueOf(0).equals(a.getViewCount()) &&
                Integer.valueOf(1).equals(a.getStatus())
        ));
    }

    // ========== getArticleDetail ==========

    @Test
    void getArticleDetail_cacheHit_returnsCached() {
        ArticleVO cached = new ArticleVO();
        cached.setId(1L);
        cached.setTitle("Cached");

        when(redisUtils.get("article:detail:1")).thenReturn(cached);

        ArticleVO result = articleService.getArticleDetail(1L, 10L);

        assertEquals("Cached", result.getTitle());
        verify(articleMapper, never()).selectById(anyLong());
    }

    @Test
    void getArticleDetail_cacheMiss_queriesDb() {
        when(redisUtils.get("article:detail:1")).thenReturn(null);

        Article article = new Article();
        article.setId(1L);
        article.setUserId(1L);
        article.setTitle("DB Article");
        article.setViewCount(5);
        article.setStatus(1);

        User user = new User();
        user.setId(1L);
        user.setUsername("author");

        when(articleMapper.selectById(1L)).thenReturn(article);
        when(userMapper.selectById(1L)).thenReturn(user);

        ArticleVO result = articleService.getArticleDetail(1L, 10L);

        assertEquals("DB Article", result.getTitle());
        assertEquals(Integer.valueOf(6), result.getViewCount());
        verify(redisUtils).set(eq("article:detail:1"), any(), eq(30L), eq(java.util.concurrent.TimeUnit.MINUTES));
    }

    @Test
    void getArticleDetail_notFound_throws() {
        when(redisUtils.get("article:detail:999")).thenReturn(null);
        when(articleMapper.selectById(999L)).thenReturn(null);

        assertThrows(RuntimeException.class, () -> articleService.getArticleDetail(999L, null));
    }

    // ========== getArticleList ==========

    @Test
    void getArticleList_returnsPaginatedResults() {
        Page<Article> articlePage = new Page<>(1, 10);
        Article article = new Article();
        article.setId(1L);
        article.setUserId(1L);
        article.setCategoryId(1L);
        article.setStatus(1);
        articlePage.setRecords(List.of(article));
        articlePage.setTotal(1);

        when(articleMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(articlePage);

        User user = new User();
        user.setId(1L);
        user.setUsername("author");
        when(userMapper.selectBatchIds(anyCollection())).thenReturn(List.of(user));

        Category cat = new Category();
        cat.setId(1L);
        cat.setName("Tech");
        when(categoryMapper.selectBatchIds(anyCollection())).thenReturn(List.of(cat));

        Page<ArticleVO> result = articleService.getArticleList(1, 10, null);

        assertEquals(1, result.getRecords().size());
        assertEquals("author", result.getRecords().get(0).getUsername());
        assertEquals("Tech", result.getRecords().get(0).getCategoryName());
    }

    @Test
    void getArticleList_emptyPage() {
        Page<Article> emptyPage = new Page<>(1, 10);
        emptyPage.setRecords(Collections.emptyList());
        emptyPage.setTotal(0);

        when(articleMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(emptyPage);

        Page<ArticleVO> result = articleService.getArticleList(1, 10, null);

        assertTrue(result.getRecords().isEmpty());
        assertEquals(0, result.getTotal());
    }

    // ========== updateArticle ==========

    @Test
    void updateArticle_success() {
        Article article = new Article();
        article.setId(1L);
        article.setUserId(1L);
        article.setTitle("Old Title");

        when(articleMapper.selectById(1L)).thenReturn(article);

        ArticleDTO dto = new ArticleDTO();
        dto.setTitle("New Title");
        dto.setContent("New Content");

        articleService.updateArticle(1L, 1L, dto);

        verify(articleMapper).updateById(argThat(a -> "New Title".equals(a.getTitle())));
        verify(redisUtils).delete("article:detail:1");
    }

    @Test
    void updateArticle_notFound_throws() {
        when(articleMapper.selectById(999L)).thenReturn(null);

        ArticleDTO dto = new ArticleDTO();
        assertThrows(RuntimeException.class, () -> articleService.updateArticle(1L, 999L, dto));
    }

    @Test
    void updateArticle_notOwner_throws() {
        Article article = new Article();
        article.setId(1L);
        article.setUserId(1L);

        when(articleMapper.selectById(1L)).thenReturn(article);

        ArticleDTO dto = new ArticleDTO();
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> articleService.updateArticle(2L, 1L, dto));
        assertEquals("无权修改此文章", ex.getMessage());
    }

    // ========== deleteArticle ==========

    @Test
    void deleteArticle_success() {
        Article article = new Article();
        article.setId(1L);
        article.setUserId(1L);

        when(articleMapper.selectById(1L)).thenReturn(article);

        articleService.deleteArticle(1L, 1L);

        verify(articleMapper).deleteById(1L);
        verify(redisUtils).delete("article:detail:1");
    }

    @Test
    void deleteArticle_notOwner_throws() {
        Article article = new Article();
        article.setId(1L);
        article.setUserId(1L);

        when(articleMapper.selectById(1L)).thenReturn(article);

        assertThrows(RuntimeException.class, () -> articleService.deleteArticle(2L, 1L));
    }
}
